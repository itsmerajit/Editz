package com.editz.data.model

data class Project(
    val id: String,
    val name: String,
    val thumbnailUrl: String?,
    val createdAt: Long,
    val folderName: String,
    val duration: Long = 0
) 