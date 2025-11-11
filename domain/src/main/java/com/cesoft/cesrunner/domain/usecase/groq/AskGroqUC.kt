package com.cesoft.cesrunner.domain.usecase.groq

import com.cesoft.cesrunner.domain.repository.RepositoryContract

class AskGroqUC(private val repository: RepositoryContract) {
    suspend operator fun invoke(prompt: String) =
        repository.askGroq(prompt)
}