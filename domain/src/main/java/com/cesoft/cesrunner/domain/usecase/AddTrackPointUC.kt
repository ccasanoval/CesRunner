package com.cesoft.cesrunner.domain.usecase

import com.cesoft.cesrunner.domain.entity.TrackPointDto
import com.cesoft.cesrunner.domain.repository.RepositoryContract

class AddTrackPointUC(private val repository: RepositoryContract) {
    suspend operator fun invoke(id: Long, data: TrackPointDto) = repository.addTrackPoint(id, data)
}
