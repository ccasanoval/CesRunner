package com.cesoft.cesrunner.domain.usecase.ai

import com.cesoft.cesrunner.domain.repository.RepositoryContract

class FilterTracksUC(private val repository: RepositoryContract) {
    suspend operator fun invoke(name: String? = null, distance: Int? = null) =
        repository.filterTracks(name, distance)
}
