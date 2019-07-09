package com.koalatea.sedaily.feature.relatedlinks.event

data class ValidationStatus(
        val isTitleValid: Boolean,
        val isUrlValid: Boolean
)