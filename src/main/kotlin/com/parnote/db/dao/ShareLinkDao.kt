package com.parnote.db.dao

import com.parnote.db.Dao
import com.parnote.db.model.ShareLink
import com.parnote.model.Result
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface ShareLinkDao : Dao<ShareLink> {
    fun addShareLink(
        shareLink: ShareLink,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun deleteByTokenID(
        tokenID: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )
}