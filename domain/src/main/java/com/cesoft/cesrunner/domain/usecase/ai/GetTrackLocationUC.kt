package com.cesoft.cesrunner.domain.usecase.ai

import com.cesoft.cesrunner.domain.repository.RepositoryContract

class GetTrackLocationUC(private val repository: RepositoryContract) {
    suspend operator fun invoke(trackId: Long) =
        repository.getLastLocation(trackId)
}
