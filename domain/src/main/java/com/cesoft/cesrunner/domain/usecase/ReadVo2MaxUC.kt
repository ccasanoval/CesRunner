package com.cesoft.cesrunner.domain.usecase

import com.cesoft.cesrunner.domain.repository.RepositoryContract

class ReadVo2MaxUC(private val repository: RepositoryContract) {
    suspend operator fun invoke() = repository.readVo2Max()
}
