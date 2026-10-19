package com.cesoft.cesrunner.domain.usecase

import com.cesoft.cesrunner.domain.entity.TrackProgress
import com.cesoft.cesrunner.domain.repository.RepositoryContract

class AddTrackPointUC(private val repository: RepositoryContract) {
    suspend operator fun invoke(progress: TrackProgress): Result<Unit> {
        val point = progress.lastPoint ?: return Result.success(Unit)
        return repository.addTrackPoint(
            id = progress.trackId,
            point = point,
            distance = progress.distance.toInt(),
            timeIni = progress.timeIni,
        )
    }
}
