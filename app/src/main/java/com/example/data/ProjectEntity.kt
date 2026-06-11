package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "project_items")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String,
    val mediaUrl: String,
    val isVideo: Boolean = false
)
