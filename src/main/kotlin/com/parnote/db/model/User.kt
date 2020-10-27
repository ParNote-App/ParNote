package com.parnote.db.model

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val password: String,
    val ipAddress: String,
    val permissionID: Int = 0,
    val emailVerified: Boolean = false
)