package com.yunfie.illustia.settings.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface SettingsDao {
    @Query("SELECT * FROM search_history ORDER BY position ASC")
    List<SearchHistoryEntity> getSearchHistory();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSearchHistory(List<SearchHistoryEntity> items);

    @Query("DELETE FROM search_history")
    void clearSearchHistory();

    @Query("SELECT * FROM favorite_tags ORDER BY position ASC")
    List<FavoriteTagEntity> getFavoriteTags();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavoriteTags(List<FavoriteTagEntity> items);

    @Query("DELETE FROM favorite_tags")
    void clearFavoriteTags();

    @Query("SELECT * FROM view_history ORDER BY position ASC")
    List<ViewHistoryEntity> getViewHistory();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertViewHistory(List<ViewHistoryEntity> items);

    @Query("DELETE FROM view_history")
    void clearViewHistory();

    @Query("SELECT * FROM accounts ORDER BY position ASC")
    List<AccountEntity> getAccounts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAccounts(List<AccountEntity> items);

    @Query("DELETE FROM accounts")
    void clearAccounts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertSavedIllust(SavedIllustEntity item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertSavedIllustPages(List<SavedIllustPageEntity> items);

    @Query("SELECT * FROM saved_illusts ORDER BY savedAt DESC")
    List<SavedIllustEntity> getSavedIllusts();

    @Transaction
    @Query("SELECT * FROM saved_illusts WHERE illustId = :illustId LIMIT 1")
    SavedIllustWithPages getSavedIllust(long illustId);

    @Query("DELETE FROM saved_illust_pages WHERE illustId = :illustId")
    void deleteSavedIllustPages(long illustId);

    @Query("DELETE FROM saved_illusts WHERE illustId = :illustId")
    void deleteSavedIllust(long illustId);

    @Query("DELETE FROM saved_illust_pages")
    void clearSavedIllustPages();

    @Query("DELETE FROM saved_illusts")
    void clearSavedIllusts();
}
