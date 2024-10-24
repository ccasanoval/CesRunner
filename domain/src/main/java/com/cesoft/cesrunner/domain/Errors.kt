package com.cesoft.cesrunner.domain

sealed class Errors {
    data object UnknownException: Errors()
    data object NetworkException: Errors()
}