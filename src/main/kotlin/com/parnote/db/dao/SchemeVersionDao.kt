package com.parnote.db.dao

import com.parnote.db.Dao
import com.parnote.db.model.SchemeVersion
import com.parnote.model.Result
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface SchemeVersionDao : Dao<SchemeVersion> {
    fun add(
        sqlConnection: SQLConnection,
        schemeVersion: SchemeVersion,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ): SQLConnection

    fun getLastSchemeVersion(
        sqlConnection: SQLConnection,
        handler: (schemeVersion: SchemeVersion?, asyncResult: AsyncResult<*>) -> Unit
    )
}