package com.catalent.auth.domain.model

/**
 * Domain model representing an authenticated session.
 */
data class AuthSession(
    val userId: String = "005000000000000AAA",
    val orgId: String = "00D000000000000AAA",
    val username: String,
    val accessToken: String,
    val refreshToken: String,
    val instanceUrl: String,
    val communityId: String,
    val communityUrl: String,
    val idUrl: String
)
