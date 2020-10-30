package com.parnote

// last ID 0
enum class ErrorCode {
    SCHEME_VERSION_ADD_ERROR,
    UNKNOWN_ERROR,
    UNKNOWN_ERROR_2,

    REGISTER_EMAIL_INVALID,
    REGISTER_USERNAME_EMPTY,
    REGISTER_USERNAME_LONG,
    REGISTER_USERNAME_SHORT,
    REGISTER_USERNAME_INVALID,
    REGISTER_NOT_ACCEPTED_TERMS,


    RESET_PASSWORD_USERNAME_OR_EMAIL_INVALID,
    RESET_PASSWORD_RECAPTCHA_INVALID,
    RESET_PASSWORD_USER_NOT_EXISTS,


}