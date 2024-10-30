package com.cesoft.cesrunner.domain.usecase

import com.cesoft.cesrunner.domain.repository.RepositoryContract

class StopLocationUpdatesUC(private val repository: RepositoryContract)  {
    operator fun invoke() = repository.stopLocationUpdates()
}
