package com.example.android.architecture.blueprints.todoapp.data.source

import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultTasksRepositoryTest {

    private val task1 = Task("Title1", "Description1")
    private val task2 = Task("Title2", "Description2")
    private val task3 = Task("Title3", "Description3")

    private val remoteTasks = listOf(task1, task2).sortedBy { it.id }
    private val localTasks = listOf(task2).sortedBy { it.id }

    private lateinit var tasksRemoteDataSource: TasksDataSource
    private lateinit var tasksLocalDataSource: TasksDataSource

    private lateinit var tasksRepository: DefaultTasksRepository

    @Before
    fun createRepository() {
        tasksRemoteDataSource = FakeDataSource(remoteTasks.toMutableList())
        tasksLocalDataSource = FakeDataSource(localTasks.toMutableList())

        tasksRepository = DefaultTasksRepository(
            tasksRemoteDataSource,
            tasksLocalDataSource,
            Dispatchers.Main
        )
    }

    @Test
    fun getTasks_requestsAllTasksFromRemoteDataSource() = runTest {
        // Act
        val result = tasksRepository.getTasks(true) as Result.Success

        // Assert
        assertEquals(remoteTasks, result.data)
    }

    @Test
    fun getTasks_errorWhenRequestsAllTasksFromRemoteDataSource() = runTest {
        // Arrange
        tasksRemoteDataSource = FakeDataSource(null)
        tasksRepository = DefaultTasksRepository(
            tasksRemoteDataSource,
            tasksLocalDataSource,
            Dispatchers.Main
        )

        // Act
        val result = tasksRepository.getTasks(true) as Result.Error

        // Assert
        assertEquals("Tasks not found", result.exception.message)
    }

    @Test
    fun saveTask_addNewTaskToRemoteAndLocalDataSource() = runTest {
        // Arrange
        val expected = listOf(task2, task3)

        // Act
        tasksRepository.saveTask(task3)

        // Assert
        val result = tasksLocalDataSource.getTasks() as Result.Success
        assertEquals(expected, result.data)
    }
}