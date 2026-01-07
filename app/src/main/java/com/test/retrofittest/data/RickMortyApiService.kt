package com.test.retrofittest.data

import com.test.retrofittest.data.response.RickMortyResponseWrapper
import retrofit2.http.GET
import retrofit2.http.Query

interface RickMortyApiService {

    @GET("/api/character/")
    suspend fun getCharacters(@Query("page") page: Int): RickMortyResponseWrapper

}