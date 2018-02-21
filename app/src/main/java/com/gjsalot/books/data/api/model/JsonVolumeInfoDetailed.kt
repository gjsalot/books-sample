package com.gjsalot.books.data.api.model

data class JsonVolumeInfoDetailed(
        val title: String,
        val authors: List<String>,
        val publishedDate: String,
        val publisher: String,
        val pageCount: Int,
        val imageLinks: JsonImageLinks?,
        val description: String,
        val industryIdentifiers: List<JsonIndustryIdentifier>?
)
