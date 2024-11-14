package com.cesoft.cesrunner.domain.usecase

import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.repository.RepositoryContract
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.transform

class ReadCurrentTrackFlowUC(private val repository: RepositoryContract) {
    @OptIn(FlowPreview::class)
    suspend operator fun invoke(): Result<Flow<TrackDto?>> {
        val res = repository.readCurrentTrackIdFlow()
        val flow = res.getOrNull()?.filterNotNull()
        if(res.isSuccess && flow != null) {
            return Result.success(
                flow.transform { id ->
                    repository.readTrackFlow(id).getOrNull()?.let { emit(it) }
                }.flattenConcat()//.stateIn(WhileSubscribed(5_000L)) ?
            )
        }
        return Result.failure(res.exceptionOrNull() ?: AppError.NotFound)
    }
}
