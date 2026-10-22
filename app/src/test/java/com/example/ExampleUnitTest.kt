package com.example

import com.example.data.local.SampleUrduNovels
import com.example.data.model.NovelCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSampleUrduNovels_loadedSuccessfully() {
        val novels = SampleUrduNovels.novelsList
        assertTrue("Novels list should not be empty", novels.isNotEmpty())
        assertTrue("Novels list should have at least 5 novels", novels.size >= 5)

        // Check Peer-e-Kamil
        val peerEKamil = novels.firstOrNull { it.id == "novel_peer_e_kamil" }
        assertNotNull(peerEKamil)
        assertEquals("پیرِ کامل (ص)", peerEKamil?.title)
        assertEquals("عمیرہ احمد", peerEKamil?.author)

        // Check chapters
        val peerChapters = SampleUrduNovels.chaptersMap["novel_peer_e_kamil"]
        assertNotNull(peerChapters)
        assertTrue(peerChapters!!.isNotEmpty())
        assertTrue(peerChapters.first().content.contains("امامہ ہاشم"))
    }

    @Test
    fun testNovelCategories_mapping() {
        assertEquals(NovelCategory.ROMANCE, NovelCategory.fromUrdu("رومانوی"))
        assertEquals(NovelCategory.SPIRITUAL, NovelCategory.fromUrdu("روحانی"))
        assertEquals(NovelCategory.SOCIAL, NovelCategory.fromUrdu("معاشرتی"))
        assertEquals(NovelCategory.ISLAHI, NovelCategory.fromUrdu("اصلاحی"))
    }

    @Test
    fun testTextPaginator_findPageIndexForCharOffset() {
        val samplePages = listOf(
            com.example.data.model.BookPage(
                pageIndex = 0,
                pageNumber = 1,
                startCharOffset = 0,
                endCharOffset = 100,
                text = "صفحہ 1",
                isFirstPageOfChapter = true,
                isLastPageOfChapter = false
            ),
            com.example.data.model.BookPage(
                pageIndex = 1,
                pageNumber = 2,
                startCharOffset = 100,
                endCharOffset = 250,
                text = "صفحہ 2",
                isFirstPageOfChapter = false,
                isLastPageOfChapter = true
            )
        )

        assertEquals(0, com.example.util.TextPaginator.findPageIndexForCharOffset(samplePages, 0))
        assertEquals(0, com.example.util.TextPaginator.findPageIndexForCharOffset(samplePages, 50))
        assertEquals(1, com.example.util.TextPaginator.findPageIndexForCharOffset(samplePages, 100))
        assertEquals(1, com.example.util.TextPaginator.findPageIndexForCharOffset(samplePages, 200))
        assertEquals(1, com.example.util.TextPaginator.findPageIndexForCharOffset(samplePages, 500)) // clamps to last page
    }
}
