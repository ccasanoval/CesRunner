package com.cesoft.cesrunner.domain.usecase

import com.cesoft.cesrunner.domain.ID_NULL
import com.cesoft.cesrunner.domain.repository.RepositoryContract

class DeleteCurrentTrackUC(private val repository: RepositoryContract) {
    suspend operator fun invoke() = repository.saveCurrentTrack(ID_NULL)
}
