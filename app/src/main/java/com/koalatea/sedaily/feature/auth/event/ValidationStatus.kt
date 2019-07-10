package com.koalatea.sedaily.feature.auth.event

data class ValidationStatus(
        val isUsernameValid: Boolean,
        val isEmailValid: Boolean,
        val isPasswordValid: Boolean
)