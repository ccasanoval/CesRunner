package com.cesoft.cesrunner.domain.usecase

import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.repository.RepositoryContract

class CreateTrackUC(private val repository: RepositoryContract) {
    suspend operator fun invoke(data: TrackDto) = repository.createTrack(data)
}
