package com.motosapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String,
)

data class RegisterRequest(
    val username:  String,
    val email:     String,
    val password:  String,
    @SerializedName("password2") val password2: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name")  val lastName: String,
)

data class TokenRefreshRequest(
    val refresh: String,
)

data class LogoutRequest(
    val refresh: String,
)

data class AuthResponseDto(
    val access:   String,
    val refresh:  String,
    @SerializedName("user_id")  val userId:  Int,
    val username: String,
    val email:    String,
    @SerializedName("is_staff") val isStaff: Boolean,
)

data class TokenRefreshResponseDto(
    val access:  String,
    val refresh: String?,   // con ROTATE_REFRESH_TOKENS=True también devuelve nuevo refresh
)