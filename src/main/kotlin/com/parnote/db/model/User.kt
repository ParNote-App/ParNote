package com.parnote.db.model

data class User(
    val id: Int,
    val name: String,
    val surname: String,
    val username: String,
    val email: String,
    val password: String,
    val ipAddress: String,
    val permissionID: Int = 0,
    val emailVerified: Boolean = false
)