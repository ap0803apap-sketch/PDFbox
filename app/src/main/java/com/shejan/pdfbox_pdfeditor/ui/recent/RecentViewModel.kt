package com.shejan.pdfbox_pdfeditor.ui.recent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.shejan.pdfbox_pdfeditor.core.AppDatabase
import com.shejan.pdfbox_pdfeditor.model.RecentFile

class RecentViewModel(application: Application) : AndroidViewModel(application) {
    private val recentFileDao = AppDatabase.getDatabase(application).recentFileDao()
    val allRecentFiles: LiveData<List<RecentFile>> = recentFileDao.allRecentFiles

    fun deleteRecentFile(recentFile: RecentFile) {
        AppDatabase.databaseWriteExecutor.execute { recentFileDao.delete(recentFile) }
    }
}
