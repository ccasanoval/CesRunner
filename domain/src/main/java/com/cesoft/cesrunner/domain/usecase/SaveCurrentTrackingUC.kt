package com.cesoft.cesrunner.domain.usecase

import com.cesoft.cesrunner.domain.entity.CurrentTrackingDto
import com.cesoft.cesrunner.domain.repository.RepositoryContract

class SaveCurrentTrackingUC(private val repository: RepositoryContract) {
    suspend operator fun invoke(data: CurrentTrackingDto) = repository.saveCurrentTracking(data)
}
