package com.parnote.model

import com.parnote.ErrorCode

open class ConnectionError(override val errorCode: ErrorCode) : Error(errorCode)