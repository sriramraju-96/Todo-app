package com.example.todo.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.todo.data.models.ToDoData

@Dao
interface ToDoDao {

    @Query("SELECT * FROM TODO_TABLE ORDER BY id ASC")
    fun getAllData() : LiveData<List<ToDoData>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertData(todoData: ToDoData)

    @Update
    suspend fun updateData(todoData: ToDoData)

    @Delete
    suspend fun deleteItem(todoData: ToDoData)

    @Query("DELETE FROM TODO_TABLE")
    suspend fun deleteAll()

    @Query("SELECT * FROM TODO_TABLE WHERE title LIKE :searchQuery")
    fun searchDataBase(searchQuery : String) : LiveData<List<ToDoData>>

    @Query("SELECT * FROM TODO_TABLE ORDER BY CASE WHEN priority LIKE 'H%' THEN 1 WHEN priority LIKE 'M%' THEN 2 WHEN priority LIKE 'L%' THEN 3 END " )
    fun sortByHighPriority() : LiveData<List<ToDoData>>

    @Query("SELECT * FROM TODO_TABLE ORDER BY CASE WHEN priority LIKE 'L%' THEN 1 WHEN priority LIKE 'M%' THEN 2 WHEN priority LIKE 'H%' THEN 3 END " )
    fun sortByLowPriority() : LiveData<List<ToDoData>>
}