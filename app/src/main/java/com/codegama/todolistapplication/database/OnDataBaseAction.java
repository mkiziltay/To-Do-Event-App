package com.codegama.todolistapplication.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.codegama.todolistapplication.model.Task;

import java.util.List;

@Dao
public interface OnDataBaseAction {

    @Query("SELECT * FROM Task")
    List<Task> getAllTasksList();

    @Query("DELETE FROM Task")
    void truncateTheList();

    @Insert
    void insertDataIntoTaskList(Task task);

    @Query("DELETE FROM Task WHERE taskId = :taskId")
    void deleteTaskFromId(int taskId);

    @Query("SELECT * FROM Task WHERE taskId = :taskId")
    Task selectDataFromAnId(int taskId);

    //@Query("SELECT customerId FROM tb_customer ORDER BY customerId DESC LIMIT 1")
    @Query("SELECT taskId FROM Task ORDER BY taskId DESC LIMIT 1")
    int getLastItemId();

    @Query("UPDATE Task SET taskTitle = :taskTitle, taskDescription = :taskDescription, date = :taskDate, " +
            "lastAlarm = :taskTime, precedence = :taskPriority, repeat = :repeatPeriod WHERE taskId = :taskId")
    void updateAnExistingRow(int taskId, String taskTitle, String taskDescription , String taskDate, String taskTime,
                             int taskPriority,int repeatPeriod);

    @Query("UPDATE Task SET date = :taskDate, " +
            "lastAlarm = :taskTime WHERE taskId = :taskId")
    void updateAnExistingTaskTime(int taskId, String taskDate, String taskTime);

}
