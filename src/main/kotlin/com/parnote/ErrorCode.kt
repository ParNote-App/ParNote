package com.parnote

enum class ErrorCode {
    UNKNOWN_ERROR,
    UNKNOWN_ERROR_2,
    UNKNOWN_ERROR_3,
    UNKNOWN_ERROR_4,
    UNKNOWN_ERROR_5,
    UNKNOWN_ERROR_6,
    UNKNOWN_ERROR_7,
    UNKNOWN_ERROR_8,
    UNKNOWN_ERROR_9,


    REGISTER_NAME_EMPTY,
    REGISTER_NAME_SHORT,
    REGISTER_NAME_LONG,
    REGISTER_SURNAME_EMPTY,
    REGISTER_SURNAME_SHORT,
    REGISTER_SURNAME_LONG,
    REGISTER_NAME_INVALID,
    REGISTER_SURNAME_INVALID,
    REGISTER_USERNAME_EMPTY,
    REGISTER_USERNAME_LONG,
    REGISTER_USERNAME_SHORT,
    REGISTER_USERNAME_INVALID,
    REGISTER_EMAIL_EMPTY,
    REGISTER_EMAIL_INVALID,
    REGISTER_PASSWORD_EMPTY,
    REGISTER_PASSWORD_INVALID,
    REGISTER_NOT_ACCEPTED_TERMS,

    RESET_PASSWORD_USERNAME_OR_EMAIL_INVALID,
    RESET_PASSWORD_RECAPTCHA_INVALID,
    RESET_PASSWORD_USER_NOT_EXISTS,

    EMAIL_VERIFICATION_INVALID_TOKEN,

    TAKEN_EMAIL_ERROR,
    TAKEN_USERNAME_ERROR,

    RECAPTCHA_NOT_VALID,
}