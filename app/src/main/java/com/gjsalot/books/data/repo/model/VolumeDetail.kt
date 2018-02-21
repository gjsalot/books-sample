package com.gjsalot.books.data.repo.model

data class VolumeDetail(
        val thumbnailUrl: String?,
        val title: String,
        val authors: List<String>,
        val publisher: String,
        val publishedDate: String,
        val pages: Int,
        val descriptionHtml: String,
        val averageRating: Float?,
        val numRatings: Int?,
        val abeBooksUrl: String?
)
