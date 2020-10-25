package com.parnote.db.dao

import com.parnote.db.Dao
import com.parnote.model.Result
import com.parnote.model.SchemeVersion
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface SchemeVersionDao : Dao<SchemeVersion> {
    override fun add(schemeVersion: SchemeVersion, handler: (result: Result) -> Unit)

    fun add(
        sqlConnection: SQLConnection,
        schemeVersion: SchemeVersion,
        handler: (result: Result, asyncResult: AsyncResult<*>) -> Unit
    ): SQLConnection

    fun getLastSchemeVersion(
        sqlConnection: SQLConnection,
        handler: (schemeVersion: SchemeVersion?, asyncResult: AsyncResult<*>) -> Unit
    )
}