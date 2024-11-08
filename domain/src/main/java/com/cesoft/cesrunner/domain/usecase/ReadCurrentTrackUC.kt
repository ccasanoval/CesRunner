package com.cesoft.cesrunner.domain.usecase

import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.repository.RepositoryContract

class ReadCurrentTrackUC(private val repository: RepositoryContract) {
    suspend operator fun invoke(): Result<TrackDto> {
        return repository.readCurrentTrack()
//        return repository.readCurrentTrackId().getOrNull()?.let { id ->
//            repository.readTrack(id).getOrNull()?.let {
//                Result.success(it)
//            } ?: run {
//                Result.failure(AppError.NotFound)
//            }
//        } ?: run {
//            Result.failure(AppError.NotFound)
//        }
    }
}
