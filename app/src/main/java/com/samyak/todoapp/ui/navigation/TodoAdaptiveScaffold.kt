package com.samyak.todoapp.ui.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.samyak.todoapp.data.TodoDatabase
import com.samyak.todoapp.data.TaskRepository
import com.samyak.todoapp.ui.screens.TaskDetailScreen
import com.samyak.todoapp.ui.screens.TaskListScreen
import com.samyak.todoapp.ui.viewmodel.TaskViewModel
import com.samyak.todoapp.ui.viewmodel.TaskViewModelFactory

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TodoAdaptiveScaffold(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val database = remember { TodoDatabase.getDatabase(context) }
    val repository = remember { TaskRepository(database.taskDao) }
    val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(repository))
    
    val backStack = rememberNavBackStack(TodoRoute.TaskList as NavKey)
    
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { 
            if (backStack.size > 1) {
                backStack.removeAt(backStack.size - 1)
            }
        },
        sceneStrategy = listDetailStrategy,
        entryProvider = entryProvider {
            entry<TodoRoute.TaskList>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        TaskDetailScreen(
                            taskId = null,
                            viewModel = taskViewModel,
                            onBack = { /* No-op for placeholder */ }
                        )
                    }
                )
            ) {
                TaskListScreen(
                    viewModel = taskViewModel,
                    onTaskClick = { id ->
                        backStack.addDetail(TodoRoute.TaskDetail(id))
                    },
                    onAddTaskClick = {
                        backStack.addDetail(TodoRoute.TaskDetail(null))
                    }
                )
            }
            entry<TodoRoute.TaskDetail>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { route ->
                TaskDetailScreen(
                    taskId = route.taskId,
                    viewModel = taskViewModel,
                    onBack = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.size - 1)
                        }
                    }
                )
            }
        }
    )
}

private fun MutableList<NavKey>.addDetail(detailRoute: TodoRoute.TaskDetail) {
    // Remove any existing detail routes to ensure only one is in the stack
    removeAll { it is TodoRoute.TaskDetail }
    add(detailRoute)
}
