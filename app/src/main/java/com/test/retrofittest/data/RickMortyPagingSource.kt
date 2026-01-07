package com.test.retrofittest.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.test.retrofittest.presentation.CharacterModel
import java.io.IOException
import javax.inject.Inject

class RickMortyPagingSource @Inject constructor(private val apiService: RickMortyApiService):
    PagingSource<Int, CharacterModel>(){
    override fun getRefreshKey(state: PagingState<Int, CharacterModel>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CharacterModel> {
        return try {

            val page = params.key ?: 1
            val response = apiService.getCharacters(page)
            val chatacter = response.results

            val prevKey = if (page > 0) page - 1 else null
            val nextKey = if (response.information.next != null) page + 1 else null

            LoadResult.Page(
                data = chatacter.map { it.toPresentation() },
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        }
    }

}