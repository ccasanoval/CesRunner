package com.cesoft.cesrunner.domain.usecase

import com.cesoft.cesrunner.domain.repository.RepositoryContract

class GetLocationUC(private val repository: RepositoryContract)  {
    operator fun invoke() = repository.getLastKnownLocation()
}
