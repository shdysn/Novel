package com.example.data.network

import com.example.data.model.Chapter
import com.example.data.model.Novel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface SupabaseApiService {

    @GET("novels?select=*&order=created_at.desc")
    suspend fun getNovels(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String
    ): Response<List<Novel>>

    @GET("chapters?select=*")
    suspend fun getChapters(
        @Query("novel_id") novelIdFilter: String, // e.g. "eq.xxxx"
        @Query("order") order: String = "chapter_number.asc",
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String
    ): Response<List<Chapter>>

    @Headers("Prefer: return=representation")
    @POST("novels")
    suspend fun pushNovel(
        @Body novel: Novel,
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String
    ): Response<List<Novel>>

    @Headers("Prefer: return=representation")
    @POST("chapters")
    suspend fun pushChapters(
        @Body chapters: List<Chapter>,
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String
    ): Response<List<Chapter>>

    companion object {
        fun createClient(baseUrl: String): SupabaseApiService? {
            return try {
                var cleanUrl = baseUrl.trim()
                if (!cleanUrl.endsWith("/")) cleanUrl += "/"
                if (!cleanUrl.contains("/rest/v1/")) {
                    cleanUrl += "rest/v1/"
                }

                val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }

                val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(12, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()

                Retrofit.Builder()
                    .baseUrl(cleanUrl)
                    .client(okHttpClient)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()
                    .create(SupabaseApiService::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}
