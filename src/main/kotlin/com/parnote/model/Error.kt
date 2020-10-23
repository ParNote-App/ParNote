package com.parnote.model

import com.parnote.ErrorCode

open class Error(open val errorCode: ErrorCode) : Result()