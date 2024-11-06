package com.cesoft.cesrunner.domain.usecase

import com.cesoft.cesrunner.domain.repository.RepositoryContract

class RequestLocationUpdatesUC(private val repository: RepositoryContract)  {
    operator fun invoke(minInterval: Long, minDistance: Float) =
        repository.requestLocationUpdates(minInterval, minDistance)
}
