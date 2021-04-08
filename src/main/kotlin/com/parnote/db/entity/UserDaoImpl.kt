package com.parnote.db.entity

import com.parnote.db.DaoImpl
import com.parnote.db.dao.UserDao
import com.parnote.db.model.User
import com.parnote.model.Result
import com.parnote.model.Successful
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection
import org.apache.commons.codec.digest.DigestUtils
import java.util.*

class UserDaoImpl(override val tableName: String = "user") : DaoImpl(), UserDao {

    override fun init(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `name` varchar(255) NOT NULL,
              `surname` varchar(255) NOT NULL,
              `username` varchar(16) NOT NULL UNIQUE,
              `email` varchar(255) NOT NULL UNIQUE,
              `password` varchar(255) NOT NULL,
              `permission_id` int(11) NOT NULL,
              `registered_ip` varchar(255) NOT NULL,
              `secret_key` text NOT NULL,
              `public_key` text NOT NULL,
              `register_date` MEDIUMTEXT NOT NULL,
              `email_verified` int(1) NOT NULL DEFAULT 0,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User Table';
        """
            ) {
                handler.invoke(it)
            }
        }

    override fun isEmailExists(
        email: String,
        sqlConnection: SQLConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(email) FROM `${getTablePrefix() + tableName}` where email = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(email)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0) == 1, queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun isUsernameExists(
        username: String,
        sqlConnection: SQLConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {

        val query =
            "SELECT COUNT(username) FROM `${getTablePrefix() + tableName}` where username = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(username)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0) == 1, queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun isExistsByUsernameOrEmail(
        usernameOrEmail: String,
        sqlConnection: SQLConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(username) FROM `${getTablePrefix() + tableName}` where username = ? or email = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(usernameOrEmail).add(usernameOrEmail)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0) == 1, queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun isLoginCorrect(
        usernameOrEmail: String,
        password: String,
        sqlConnection: SQLConnection, handler: (loginCorrect: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {

        val query =
            "SELECT COUNT(`id`) FROM `${getTablePrefix() + tableName}` where (username = ? or email = ?) and password = ?"

        sqlConnection.queryWithParams(
            query,
            JsonArray().add(usernameOrEmail).add(usernameOrEmail).add(DigestUtils.md5Hex(password))
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0) == 1, queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun add(
        user: User,
        sqlConnection: SQLConnection,
        handler: (isSuccessful: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "INSERT INTO `${getTablePrefix() + tableName}` (name, surname, username, email, password, permission_id, registered_ip, secret_key, public_key, register_date) " +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?); "

        val key = Keys.keyPairFor(SignatureAlgorithm.RS256)

        sqlConnection.queryWithParams(
            query,
            JsonArray()
                .add(user.name)
                .add(user.surname)
                .add(user.username)
                .add(user.email)
                .add(DigestUtils.md5Hex(user.password))
                .add(user.permissionID)
                .add(user.ipAddress)
                .add(Base64.getEncoder().encodeToString(key.private.encoded))
                .add(Base64.getEncoder().encodeToString(key.public.encoded))
                .add(System.currentTimeMillis())
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getSecretKeyByID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (secretKey: String?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT secret_key FROM `${databaseManager.getTablePrefix() + tableName}` where `id` = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(userID)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getString(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getUserIDFromUsernameOrEmail(
        usernameOrEmail: String,
        sqlConnection: SQLConnection,
        handler: (userID: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT id FROM `${databaseManager.getTablePrefix() + tableName}` where username = ? or email = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(usernameOrEmail).add(usernameOrEmail)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun isEmailVerifiedByID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (isVerified: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(`id`) FROM `${getTablePrefix() + tableName}` where `id` = ? and email_verified = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(userID).add(1)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0) == 1, queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun makeEmailVerifiedByID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "UPDATE `${databaseManager.getTablePrefix() + tableName}` SET email_verified = ? WHERE `id` = ?"

        sqlConnection.updateWithParams(
            query,
            JsonArray().add(1).add(userID)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getEmailByID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (email: String?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT email FROM `${databaseManager.getTablePrefix() + tableName}` where `id` = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(userID)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getString(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun changePasswordByID(
        userID: Int,
        newPassword: String,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "UPDATE `${databaseManager.getTablePrefix() + tableName}` SET password = ? WHERE `id` = ?"

        sqlConnection.updateWithParams(
            query,
            JsonArray().add(DigestUtils.md5Hex(newPassword)).add(userID)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getUser(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (user: User?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT `name`, `surname`, `username`, `email`, `permission_id`, `email_verified` FROM `${databaseManager.getTablePrefix() + tableName}` where `id` = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(userID)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(
                    User(
                        userID,
                        queryResult.result().results[0].getString(0),
                        queryResult.result().results[0].getString(1),
                        queryResult.result().results[0].getString(2),
                        queryResult.result().results[0].getString(3),
                        "",
                        "",
                        queryResult.result().results[0].getInteger(4),
                        queryResult.result().results[0].getInteger(5) == 1
                    ), queryResult
                )
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun isCorrectPasswordByUserID(
        userID: Int,
        password: String,
        sqlConnection: SQLConnection,
        handler: (isCorrect: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(username) FROM `${getTablePrefix() + tableName}` where `id` = ? and `password` = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(userID).add(DigestUtils.md5Hex(password))) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0) == 1, queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun deleteByUserID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "DELETE from `${databaseManager.getTablePrefix() + tableName}` WHERE `id` = ?"

        sqlConnection.updateWithParams(query, JsonArray().add(userID)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }
}