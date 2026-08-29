package com.yunfie.illustia.settings.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "saved_illust_pages")
public class SavedIllustPageEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long illustId;
    public int pageIndex;
    public String localPath;
    public String sourceUrl;
}
