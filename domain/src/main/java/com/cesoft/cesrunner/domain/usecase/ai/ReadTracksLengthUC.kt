package com.cesoft.cesrunner.domain.usecase.ai

import com.cesoft.cesrunner.domain.repository.RepositoryContract

class ReadTracksLengthUC(private val repository: RepositoryContract) {
    suspend operator fun invoke() = repository.readVo2Max()
}
