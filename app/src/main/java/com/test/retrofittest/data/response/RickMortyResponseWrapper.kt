package com.test.retrofittest.data.response

import com.google.gson.annotations.SerializedName

data class RickMortyResponseWrapper(
    @SerializedName("info") val information: InformationResponse,
    @SerializedName("results") val results: List<CharacterResponse>,

)