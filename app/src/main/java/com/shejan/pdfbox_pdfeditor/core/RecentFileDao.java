package com.shejan.pdfbox_pdfeditor.core;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.shejan.pdfbox_pdfeditor.model.RecentFile;

import java.util.List;

@Dao
public interface RecentFileDao {
    @Query("SELECT * FROM recent_files ORDER BY lastOpened DESC LIMIT 5")
    LiveData<List<RecentFile>> getRecentFilesLimit();

    @Query("SELECT * FROM recent_files ORDER BY lastOpened DESC")
    LiveData<List<RecentFile>> getAllRecentFiles();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RecentFile recentFile);

    @Update
    void update(RecentFile recentFile);

    @Delete
    void delete(RecentFile recentFile);

    @Query("DELETE FROM recent_files")
    void deleteAll();
}
