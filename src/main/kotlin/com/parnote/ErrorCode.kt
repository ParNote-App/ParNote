package com.parnote

// last ID 0
enum class ErrorCode {
    SCHEME_VERSION_ADD_ERROR,
    UNKNOWN_ERROR,
    UNKNOWN_ERROR_2,
    UNKNOWN_ERROR_3,
    UNKNOWN_ERROR_4,

    REGISTER_EMAIL_INVALID,
    REGISTER_USERNAME_EMPTY,
    REGISTER_USERNAME_LONG,
    REGISTER_USERNAME_SHORT,
    REGISTER_USERNAME_INVALID,
    REGISTER_NOT_ACCEPTED_TERMS,

    RESETPASSWORD_PASSWORD_EMPTY,
    RESETPASSWORD_PASSWORD_SHORT,
    RESETPASSWORD_PASSWORD_LONG,
    RESETPASSWORD_PASSWORD_INVALID,

    NEWPASSWORD_EMPTY,
    NEWPASSWORD_INVALID,
    NEWPASSWORD_DOESNT_MATCH,
    NEWPASSWORD_REPEAT_EMPTY,
    NEWPASSWORD_REPEAT_INVALID,

    RECAPTCHA_NOT_VALID,

    TOKEN_IS_INVALID,


}