package com.cesoft.cesrunner.domain.usecase

import com.cesoft.cesrunner.domain.repository.RepositoryContract

class StopLocationUpdatesUC(private val repository: RepositoryContract)  {
    suspend operator fun invoke() = repository.stopLocationUpdates()
}
