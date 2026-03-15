package com.shejan.pdfbox_pdfeditor.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.shejan.pdfbox_pdfeditor.core.AppDatabase;
import com.shejan.pdfbox_pdfeditor.core.RecentFileDao;
import com.shejan.pdfbox_pdfeditor.model.RecentFile;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private final RecentFileDao recentFileDao;
    private final LiveData<List<RecentFile>> recentFiles;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        recentFileDao = db.recentFileDao();
        recentFiles = recentFileDao.getRecentFilesLimit();
    }

    public LiveData<List<RecentFile>> getRecentFiles() {
        return recentFiles;
    }

    public void insertRecentFile(RecentFile recentFile) {
        AppDatabase.databaseWriteExecutor.execute(() -> recentFileDao.insert(recentFile));
    }

    public void deleteRecentFile(RecentFile recentFile) {
        AppDatabase.databaseWriteExecutor.execute(() -> recentFileDao.delete(recentFile));
    }
}
