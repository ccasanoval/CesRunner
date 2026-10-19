package com.cesoft.cesrunner.domain

sealed class AppError: Throwable() {
    data class UnknownError(override val message: String): AppError()
    data object NetworkError: AppError() { private fun readResolve(): Any = NetworkError }
    data object NotFound: AppError() { private fun readResolve(): Any = NotFound }
    data class DataBaseError(val e: Throwable): AppError()
    data class FileError(val filename: String): AppError()
    data object MissingApiKey: AppError() { private fun readResolve(): Any = MissingApiKey }

    companion object {
        fun fromThrowable(e: Throwable): AppError = when(e) {
            is AppError -> e
            else -> UnknownError(message = e.localizedMessage ?: e.message ?: "?")
        }
    }
}