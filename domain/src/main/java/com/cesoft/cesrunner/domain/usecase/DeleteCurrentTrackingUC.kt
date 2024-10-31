package com.cesoft.cesrunner.domain.usecase

import com.cesoft.cesrunner.domain.repository.RepositoryContract

class DeleteCurrentTrackingUC(private val repository: RepositoryContract) {
    suspend operator fun invoke() = repository.saveCurrentTrack()
}
