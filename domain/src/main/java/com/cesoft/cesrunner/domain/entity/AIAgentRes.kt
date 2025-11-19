package com.cesoft.cesrunner.domain.entity

data class RunDto(
    val id: Long = -1,
    val name: String = "",
    val timeIni: Long = 0,
    val timeEnd: Long = 0,
    val distance: Int = -1,
    val distanceToLocation: Int = -1,
    val vo2Max: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
)

data class AIAgentRes(
    val msg: String = "",
    val data: List<RunDto> = listOf()
)