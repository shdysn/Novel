package com.example.data.supabase

object TypeScriptGuide {

    const val SUPABASE_SQL_SCHEMA = """-- Supabase SQL Editor میں یہ اسکرپٹ چلائیں:
-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Create 'novels' table
CREATE TABLE IF NOT EXISTS public.novels (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL,
    author TEXT NOT NULL,
    category TEXT NOT NULL,
    synopsis TEXT NOT NULL,
    rating NUMERIC(3, 2) DEFAULT 4.8,
    views_count INTEGER DEFAULT 0,
    cover_gradient_index INTEGER DEFAULT 0,
    total_chapters INTEGER DEFAULT 1,
    is_favorite BOOLEAN DEFAULT false,
    is_downloaded BOOLEAN DEFAULT true,
    is_custom_published BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 2. Create 'chapters' table
CREATE TABLE IF NOT EXISTS public.chapters (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    novel_id UUID NOT NULL REFERENCES public.novels(id) ON DELETE CASCADE,
    chapter_number INTEGER NOT NULL,
    title TEXT NOT NULL,
    content TEXT NOT NULL, -- مکمل یونیکوڈ اردو متن (Unicode Urdu Text)
    word_count INTEGER DEFAULT 0,
    read_time_minutes INTEGER DEFAULT 5,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 3. Set RLS Policies (Allow Read & Insert for Anon)
ALTER TABLE public.novels ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.chapters ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Public Read Novels" ON public.novels FOR SELECT USING (true);
CREATE POLICY "Public Insert Novels" ON public.novels FOR INSERT WITH CHECK (true);
CREATE POLICY "Public Update Novels" ON public.novels FOR UPDATE USING (true);

CREATE POLICY "Public Read Chapters" ON public.chapters FOR SELECT USING (true);
CREATE POLICY "Public Insert Chapters" ON public.chapters FOR INSERT WITH CHECK (true);
"""

    const val TYPESCRIPT_PUBLISH_SCRIPT = """// push_novels.ts
// npm install @supabase/supabase-js dotenv
// run: npx ts-node push_novels.ts

import { createClient } from '@supabase/supabase-js';
import * as dotenv from 'dotenv';
dotenv.config();

// 1. Supabase credentials
const SUPABASE_URL = process.env.SUPABASE_URL || 'https://YOUR_PROJECT.supabase.co';
const SUPABASE_KEY = process.env.SUPABASE_SERVICE_ROLE_KEY || process.env.SUPABASE_ANON_KEY || 'YOUR_KEY';

const supabase = createClient(SUPABASE_URL, SUPABASE_KEY);

interface NovelInput {
  title: string;
  author: string;
  category: string;
  synopsis: string;
  cover_gradient_index: number;
  chapters: {
    chapter_number: number;
    title: string;
    content: string; // خالص یونیکوڈ اردو متن
  }[];
}

// 2. Sample Urdu Novel in Unicode
const novelData: NovelInput = {
  title: "پیرِ کامل (ص)",
  author: "عمیرہ احمد",
  category: "روحانی و تصوف",
  synopsis: "یہ کہانی ہے زندگی کے گمبھیر اندھیروں سے نکل کر نور کی تلاش کرنے والے دو متضاد نفوس کی...",
  cover_gradient_index: 0,
  chapters: [
    {
      chapter_number: 1,
      title: "باب اول: سفر کا آغاز",
      content: "زندگی میں بعض فیصلے ایسے ہوتے ہیں جنہیں وقت نہیں انسان خود منتخب کرتا ہے۔ امامہ ہاشم نے جب پہلی بار اپنی ذات کے دائرے سے باہر جھانکا تو اسے ہر سو ایک خاموش سناٹا دکھائی دیا..."
    },
    {
      chapter_number: 2,
      title: "باب دوم: کشمکش اور حقیقت",
      content: "سالار سکندر ایک غیر معمولی ذہن کا مالک نوجوان تھا جس کے لیے دنیا کے تمام معمے محض کھیل کا حصہ تھے۔ مگر زندگی کا سب سے بڑا معمہ ابھی اس کے سامنے آنا باقی تھا..."
    }
  ]
};

async function publishNovel(novel: NovelInput) {
  console.log(`پبلشنگ شروع: ${'$'}{novel.title}...`);

  // Step A: Insert Novel
  const { data: novelRecord, error: novelError } = await supabase
    .from('novels')
    .insert({
      title: novel.title,
      author: novel.author,
      category: novel.category,
      synopsis: novel.synopsis,
      total_chapters: novel.chapters.length,
      cover_gradient_index: novel.cover_gradient_index,
      rating: 4.9,
      views_count: 1500,
      is_custom_published: true
    })
    .select()
    .single();

  if (novelError || !novelRecord) {
    console.error("ناول محفوظ کرتے ہوئے خرابی:", novelError);
    return;
  }

  console.log(`ناول شامل ہو گیا، ID: ${'$'}{novelRecord.id}`);

  // Step B: Insert Chapters
  for (const ch of novel.chapters) {
    const wordCount = ch.content.trim().split(/\s+/).length;
    const readTime = Math.max(1, Math.ceil(wordCount / 120));

    const { error: chError } = await supabase
      .from('chapters')
      .insert({
        novel_id: novelRecord.id,
        chapter_number: ch.chapter_number,
        title: ch.title,
        content: ch.content,
        word_count: wordCount,
        read_time_minutes: readTime
      });

    if (chError) {
      console.error(`باب ${'$'}{ch.chapter_number} شامل نہیں ہو سکا:`, chError);
    } else {
      console.log(`باب ${'$'}{ch.chapter_number} محفوظ ہو گیا: ${'$'}{ch.title}`);
    }
  }

  console.log("تمام ڈیٹا کامیابی سے Supabase کلاؤڈ پر پش ہو گیا!");
}

publishNovel(novelData);
"""

    const val TYPESCRIPT_FETCH_SCRIPT = """// fetch_novels.ts
// Supabase سے اردو ناولز حاصل کرنے کا اسکرپٹ
import { createClient } from '@supabase/supabase-js';

const SUPABASE_URL = 'https://YOUR_PROJECT.supabase.co';
const SUPABASE_KEY = 'YOUR_SUPABASE_ANON_KEY';
const supabase = createClient(SUPABASE_URL, SUPABASE_KEY);

async function fetchNovelsAndChapters() {
  // 1. Fetch all novels
  const { data: novels, error } = await supabase
    .from('novels')
    .select('*')
    .order('created_at', { ascending: false });

  if (error) {
    console.error("ناولز لانے میں خرابی:", error);
    return;
  }

  console.log(`کل دستیاب ناولز: ${'$'}{novels.length}`);
  novels.forEach(n => console.log(`- ${'$'}{n.title} (از ${'$'}{n.author})`));

  // 2. Fetch Chapters of first novel
  if (novels.length > 0) {
    const firstNovel = novels[0];
    const { data: chapters } = await supabase
      .from('chapters')
      .select('*')
      .eq('novel_id', firstNovel.id)
      .order('chapter_number', { ascending: true });

    console.log(`\nناول "${'$'}{firstNovel.title}" کے ابواب:`);
    chapters?.forEach(c => {
      console.log(`[باب ${'$'}{c.chapter_number}] ${'$'}{c.title} (${'$'}{c.word_count} الفاظ)`);
    });
  }
}

fetchNovelsAndChapters();
"""
}
