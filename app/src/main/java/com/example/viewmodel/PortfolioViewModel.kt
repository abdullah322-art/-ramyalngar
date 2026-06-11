package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ProjectEntity
import com.example.data.ProjectRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PortfolioViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProjectRepository

    val allProjects: StateFlow<List<ProjectEntity>>

    init {
        val projectDao = AppDatabase.getDatabase(application).projectDao()
        repository = ProjectRepository(projectDao)
        allProjects = repository.allProjects.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        seedInitialData()
    }

    private fun seedInitialData() {
        viewModelScope.launch {
            repository.allProjects.collect { current ->
                if (current.isEmpty()) {
                    repository.insert(
                        ProjectEntity(
                            title = "لوحة خشبية بشعار العمل",
                            description = "لوحة خشبية محفورة بشكل احترافي مع أدوات نجارة بارزة.",
                            category = "Wood Art",
                            mediaUrl = "android.resource://com.example/drawable/carpenter_logo_1781085806214"
                        )
                    )
                    repository.insert(
                        ProjectEntity(
                            title = "مطبخ خشب زان",
                            description = "مطبخ عصري بتصميم عملي ومساحات تخزين واسعة",
                            category = "Kitchens",
                            mediaUrl = "https://images.unsplash.com/photo-1556910103-1c02745a872e?auto=format&fit=crop&w=600&q=80"
                        )
                    )
                    repository.insert(
                        ProjectEntity(
                            title = "ديكور حائط خشبي",
                            description = "لمسة جمالية لغرفة المعيشة بتصميم ريفي حديث",
                            category = "Decor",
                            mediaUrl = "https://images.unsplash.com/photo-1600121848594-d8644e57abab?auto=format&fit=crop&w=600&q=80"
                        )
                    )
                }
                throw kotlinx.coroutines.CancellationException("Seed complete or skipped")
            }
        }
    }

    fun addProject(title: String, description: String, category: String, mediaUrl: String) {
        viewModelScope.launch {
            repository.insert(
                ProjectEntity(
                    title = title,
                    description = description,
                    category = category,
                    mediaUrl = mediaUrl
                )
            )
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            repository.delete(project)
        }
    }
}
