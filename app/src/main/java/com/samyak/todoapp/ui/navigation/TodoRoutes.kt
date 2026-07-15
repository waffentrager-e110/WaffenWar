package com.samyak.todoapp.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface TodoRoute : NavKey {
    @Serializable
    data object TaskList : TodoRoute

    @Serializable
    data class TaskDetail(val taskId: Int? = null) : TodoRoute
}
