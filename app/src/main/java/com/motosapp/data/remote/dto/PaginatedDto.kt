// data/remote/dto/PaginatedDto.kt
package com.motosapp.data.remote.dto

data class PaginatedDto<T>(
    val count:    Int,
    val next:     String?,
    val previous: String?,
    val results:  List<T>,
)