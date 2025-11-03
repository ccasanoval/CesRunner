package com.cesoft.cesrunner.domain.usecase.ai

import com.cesoft.cesrunner.domain.repository.RepositoryContract

class GetNearTracksUC(private val repository: RepositoryContract) {
    suspend operator fun invoke(lat: Double, lng: Double) =
        repository.getNearTracks(lat, lng)
}
