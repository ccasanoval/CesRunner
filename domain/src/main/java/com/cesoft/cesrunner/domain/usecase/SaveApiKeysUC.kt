package com.cesoft.cesrunner.domain.usecase

import com.cesoft.cesrunner.domain.entity.ApiKeysDto
import com.cesoft.cesrunner.domain.repository.RepositoryContract

class SaveApiKeysUC(private val repository: RepositoryContract) {
    suspend operator fun invoke(data: ApiKeysDto) = repository.saveApiKeys(data)
}
