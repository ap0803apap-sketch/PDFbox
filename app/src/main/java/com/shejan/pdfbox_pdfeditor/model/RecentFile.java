package com.shejan.pdfbox_pdfeditor.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "recent_files")
public class RecentFile {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String fileName;
    private String filePath;
    private String fileSize;
    private long lastOpened;

    public RecentFile(int id, String fileName, String filePath, String fileSize, long lastOpened) {
        this.id = id;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.lastOpened = lastOpened;
    }

    @Ignore
    public RecentFile(String fileName, String filePath, String fileSize, long lastOpened) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.lastOpened = lastOpened;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileSize() { return fileSize; }
    public void setFileSize(String fileSize) { this.fileSize = fileSize; }

    public long getLastOpened() { return lastOpened; }
    public void setLastOpened(long lastOpened) { this.lastOpened = lastOpened; }
}
