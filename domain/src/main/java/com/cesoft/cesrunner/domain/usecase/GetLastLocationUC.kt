package com.cesoft.cesrunner.domain.usecase

import com.cesoft.cesrunner.domain.repository.RepositoryContract

class GetLastLocationUC(private val repository: RepositoryContract) {
    suspend operator fun invoke(id: Long) = repository.getLastLocation(id)
}
