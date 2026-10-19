package com.cesoft.cesrunner.domain.entity

/// API keys of the LLM providers, entered by the user in settings so they never ship in the APK
data class ApiKeysDto(
    val groq: String = "",
    val gemini: String = "",
    val openAi: String = "",
    val openRouter: String = "",
    val deepSeek: String = "",
) {
    companion object {
        val Empty = ApiKeysDto()
    }
}
