package com.tech.bazaar.kmp.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoriesResponseModelV2(
    @SerialName("categories")
    var categories: ArrayList<CategoryDto>
) {

    @Serializable
    class CategoryDto(
        @SerialName("id")
        var id: String = "",
        @SerialName("title")
        var title: String,
        @SerialName("description")
        var description: String? = "",
        @SerialName("imageUrl")
        var imageUrl: String,
        @SerialName("handle")
        var handle: String? = null,
        @SerialName("isEnabled")
        var isEnabled: Boolean = false,
        @SerialName("categoryType")
        var categoryType: String = "parent",
        @SerialName("categoryFlag")
        var categoryFlag: String? = null,
        @SerialName("children")
        var children: ArrayList<CategoryDto>? = null
    )
}
