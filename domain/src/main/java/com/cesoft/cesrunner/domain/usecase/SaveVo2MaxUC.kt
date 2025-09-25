package com.cesoft.cesrunner.domain.usecase

import com.cesoft.cesrunner.domain.repository.RepositoryContract

class SaveVo2MaxUC(private val repository: RepositoryContract) {
    suspend operator fun invoke(value: Double) = repository.saveVo2Max(value)
}
