package com.parnote

enum class ErrorCode {
    UNKNOWN_ERROR_1,
    UNKNOWN_ERROR_2,
    UNKNOWN_ERROR_3,
    UNKNOWN_ERROR_4,
    UNKNOWN_ERROR_5,
    UNKNOWN_ERROR_6,
    UNKNOWN_ERROR_7,
    UNKNOWN_ERROR_8,
    UNKNOWN_ERROR_9,
    UNKNOWN_ERROR_10,
    UNKNOWN_ERROR_11,
    UNKNOWN_ERROR_12,
    UNKNOWN_ERROR_13,
    UNKNOWN_ERROR_14,
    UNKNOWN_ERROR_15,
    UNKNOWN_ERROR_16,
    UNKNOWN_ERROR_17,
    UNKNOWN_ERROR_18,
    UNKNOWN_ERROR_19,
    UNKNOWN_ERROR_20,
    UNKNOWN_ERROR_21,
    UNKNOWN_ERROR_22,
    UNKNOWN_ERROR_23,
    UNKNOWN_ERROR_24,
    UNKNOWN_ERROR_25,
    UNKNOWN_ERROR_26,

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

    LOGIN_IS_INVALID,
    LOGIN_EMAIL_NOT_VERIFIED,

    EMAIL_VERIFICATION_INVALID_TOKEN,

    TAKEN_EMAIL_ERROR,
    TAKEN_USERNAME_ERROR,

    RECAPTCHA_NOT_VALID,

    EXPIRED_TOKEN_VALIDATION,

    NEW_PASSWORD_EMPTY,
    NEW_PASSWORD_INVALID,
    NEW_PASSWORD_DOESNT_MATCH,
    NEW_PASSWORD_REPEAT_EMPTY,
    NEW_PASSWORD_REPEAT_INVALID,

    TOKEN_IS_INVALID,
}