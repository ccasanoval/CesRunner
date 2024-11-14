package com.cesoft.cesrunner.domain.usecase

import com.cesoft.cesrunner.domain.repository.RepositoryContract

class DeleteTrackUC(private val repository: RepositoryContract) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        return repository.deleteTrack(id)
    }
}
