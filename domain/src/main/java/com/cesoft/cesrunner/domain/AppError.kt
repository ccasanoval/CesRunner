package com.cesoft.cesrunner.domain

import android.net.http.NetworkException
import android.os.Build
import androidx.annotation.RequiresExtension

sealed class AppError {
    data class UnknownError(val message: String): AppError()
    data object NetworkError: AppError()
    companion object {
        fun fromThrowable(e: Throwable): AppError = when(e) {
            //is NetworkException -> NetworkError
            else -> UnknownError(message = e.localizedMessage ?: "?")
        }
    }
}