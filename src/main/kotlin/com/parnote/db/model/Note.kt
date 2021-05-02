package com.parnote.db.model

data class Note(
    val id: Int,
    val userID: Int,
    val title: String,
    val text: String,
    val lastModified: String,
    val status: Int,
    val favorite: Boolean
)