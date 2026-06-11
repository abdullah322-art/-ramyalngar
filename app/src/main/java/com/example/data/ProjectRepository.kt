package com.example.data

import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val dao: ProjectDao) {
    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()

    suspend fun insert(project: ProjectEntity) {
        dao.insertProject(project)
    }

    suspend fun delete(project: ProjectEntity) {
        dao.deleteProject(project)
    }
}
