package com.catalent.auth.data.mapper

import com.catalent.auth.domain.exceptions.AuthException
import com.salesforce.androidsdk.rest.RestResponse

object AuthExceptionMapper {

    fun map(throwable: Throwable): AuthException = when {
        throwable.message?.contains("invalid_client") == true ->
            AuthException.InvalidCredentials()

        throwable.message?.contains("expired") == true ->
            AuthException.SessionExpired()

        throwable is java.net.UnknownHostException ->
            AuthException.NetworkError()

        else ->
            AuthException.UnknownError(throwable.message ?: "Unknown error")
    }

    fun fromResponse(response: RestResponse): AuthException = when (response.statusCode) {
        401 -> AuthException.SessionExpired()
        400 -> AuthException.InvalidCredentials()
        else -> AuthException.UnknownError("HTTP ${response.statusCode}")
    }
}