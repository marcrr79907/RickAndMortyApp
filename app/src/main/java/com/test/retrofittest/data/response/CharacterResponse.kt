package com.test.retrofittest.data.response

import com.google.gson.annotations.SerializedName
import com.test.retrofittest.presentation.CharacterModel


data class CharacterResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("status") val status: String,
    @SerializedName("image") val image: String,
) {
    fun toPresentation(): CharacterModel {
        return CharacterModel(
            id = id,
            name = name,
            status = status,
            image = image
        )
    }
}