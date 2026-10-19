package com.cesoft.cesrunner.domain.usecase

import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.repository.RepositoryContract
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

class ReadCurrentTrackFlowUC(private val repository: RepositoryContract) {
    /// Database flows never complete, so when the current track changes (ie: tracking stops)
    /// the flow of the previous track must be cancelled: flatMapLatest, not flattenConcat
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(): Result<Flow<TrackDto?>> {
        val idFlow = repository.readCurrentTrackIdFlow().getOrElse {
            return Result.failure(it)
        }
        return Result.success(
            idFlow.filterNotNull().flatMapLatest { id ->
                repository.readTrackFlow(id).getOrNull() ?: emptyFlow()
            }
        )
    }
}
