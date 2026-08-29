package com.yunfie.illustia.data

import com.yunfie.illustia.models.HomeFeedKind
import com.yunfie.illustia.models.Illust
import com.yunfie.illustia.models.NovelPreview
import com.yunfie.illustia.models.PageResult
import com.yunfie.illustia.models.PixivSession
import com.yunfie.illustia.models.Restrict
import com.yunfie.illustia.models.SearchBookmarkFilter
import com.yunfie.illustia.models.SearchDuration
import com.yunfie.illustia.models.SearchSort
import com.yunfie.illustia.models.SearchTarget
import com.yunfie.illustia.models.UserPreview
import com.yunfie.illustia.models.UserProfile
import com.yunfie.illustia.models.pixiv.NotificationListResult
import com.yunfie.illustia.models.pixiv.RelatedUsersResult
import com.yunfie.illustia.settings.AppSettings

internal interface SessionRepository {
    suspend fun login(refreshToken: String): PixivSession

    suspend fun loginWithAuthorizationCode(
        code: String,
        codeVerifier: String,
    ): PixivSession

    suspend fun logout()
}

internal interface ArtworkRepository {
    suspend fun loadHome(kind: HomeFeedKind): PageResult<Illust>

    suspend fun loadRanking(mode: String): PageResult<Illust>

    suspend fun followingIllusts(restrict: Restrict): PageResult<Illust>

    suspend fun illustDetail(illustId: Long): Illust

    suspend fun relatedIllusts(illustId: Long): PageResult<Illust>

    suspend fun nextPage(nextUrl: String): PageResult<Illust>
}

internal interface SearchRepository {
    suspend fun search(
        word: String,
        sort: SearchSort,
        target: SearchTarget,
        duration: SearchDuration,
        bookmarkFilter: SearchBookmarkFilter,
        includeR18: Boolean,
    ): PageResult<Illust>

    suspend fun searchUsers(word: String): PageResult<UserPreview>

    suspend fun trendingTags(): List<String>

    suspend fun searchAutocomplete(word: String): List<String>

    suspend fun nextUserSearchPage(nextUrl: String): PageResult<UserPreview>
}

internal interface ProfileRepository {
    suspend fun userDetail(userId: Long): UserProfile

    suspend fun userIllusts(userId: Long): PageResult<Illust>

    suspend fun relatedUsers(userId: Long): RelatedUsersResult

    suspend fun nextRelatedUsersPage(nextUrl: String): RelatedUsersResult

    suspend fun followUser(
        userId: Long,
        restrict: Restrict,
    )

    suspend fun unfollowUser(userId: Long)
}

internal interface LibraryRepository {
    suspend fun bookmarks(
        userId: Long,
        restrict: Restrict,
    ): PageResult<Illust>

    suspend fun toggleBookmark(
        illust: Illust,
        restrict: Restrict,
    ): Illust

    suspend fun addNovelBookmark(
        novelId: Long,
        restrict: Restrict,
    )

    suspend fun removeNovelBookmark(novelId: Long)
}

internal interface NotificationRepository {
    suspend fun notifications(): NotificationListResult

    suspend fun notificationViewMore(notificationId: Long): NotificationListResult

    suspend fun nextNotificationPage(nextUrl: String): NotificationListResult
}

internal interface SettingsRepository {
    suspend fun readSettings(): AppSettings

    suspend fun readStartupSettings(): AppSettings

    suspend fun saveSettings(
        settings: AppSettings,
        baseSettings: AppSettings? = null,
    )

    suspend fun saveSettingsFromSync(settings: AppSettings)
}

/** Typed repository views backed by the legacy compatibility facade. */
internal class FeatureRepositories(
    facade: IllustiaRepository,
) {
    val session: SessionRepository = SessionRepositoryAdapter(facade)
    val artwork: ArtworkRepository = ArtworkRepositoryAdapter(facade)
    val search: SearchRepository = SearchRepositoryAdapter(facade)
    val profile: ProfileRepository = ProfileRepositoryAdapter(facade)
    val library: LibraryRepository = LibraryRepositoryAdapter(facade)
    val notification: NotificationRepository = NotificationRepositoryAdapter(facade)
    val settings: SettingsRepository = SettingsRepositoryAdapter(facade)
}

private class SessionRepositoryAdapter(
    private val facade: IllustiaRepository,
) : SessionRepository {
    override suspend fun login(refreshToken: String) = facade.login(refreshToken)

    override suspend fun loginWithAuthorizationCode(
        code: String,
        codeVerifier: String,
    ) = facade.loginWithAuthorizationCode(code, codeVerifier)

    override suspend fun logout() = facade.logout()
}

private class ArtworkRepositoryAdapter(
    private val facade: IllustiaRepository,
) : ArtworkRepository {
    override suspend fun loadHome(kind: HomeFeedKind) = facade.loadHome(kind)

    override suspend fun loadRanking(mode: String) = facade.loadRanking(mode)

    override suspend fun followingIllusts(restrict: Restrict) = facade.followingIllusts(restrict)

    override suspend fun illustDetail(illustId: Long) = facade.illustDetail(illustId)

    override suspend fun relatedIllusts(illustId: Long) = facade.relatedIllusts(illustId)

    override suspend fun nextPage(nextUrl: String) = facade.nextPage(nextUrl)
}

private class SearchRepositoryAdapter(
    private val facade: IllustiaRepository,
) : SearchRepository {
    override suspend fun search(
        word: String,
        sort: SearchSort,
        target: SearchTarget,
        duration: SearchDuration,
        bookmarkFilter: SearchBookmarkFilter,
        includeR18: Boolean,
    ) = facade.search(word, sort, target, duration, bookmarkFilter, includeR18)

    override suspend fun searchUsers(word: String) = facade.searchUsers(word)

    override suspend fun trendingTags() = facade.trendingTags()

    override suspend fun searchAutocomplete(word: String) = facade.searchAutocomplete(word)

    override suspend fun nextUserSearchPage(nextUrl: String) = facade.nextUserSearchPage(nextUrl)
}

private class ProfileRepositoryAdapter(
    private val facade: IllustiaRepository,
) : ProfileRepository {
    override suspend fun userDetail(userId: Long) = facade.userDetail(userId)

    override suspend fun userIllusts(userId: Long) = facade.userIllusts(userId)

    override suspend fun relatedUsers(userId: Long) = facade.relatedUsers(userId)

    override suspend fun nextRelatedUsersPage(nextUrl: String) = facade.nextRelatedUsersPage(nextUrl)

    override suspend fun followUser(
        userId: Long,
        restrict: Restrict,
    ) = facade.followUser(userId, restrict)

    override suspend fun unfollowUser(userId: Long) = facade.unfollowUser(userId)
}

private class LibraryRepositoryAdapter(
    private val facade: IllustiaRepository,
) : LibraryRepository {
    override suspend fun bookmarks(
        userId: Long,
        restrict: Restrict,
    ) = facade.bookmarks(userId, restrict)

    override suspend fun toggleBookmark(
        illust: Illust,
        restrict: Restrict,
    ) = facade.toggleBookmark(illust, restrict)

    override suspend fun addNovelBookmark(
        novelId: Long,
        restrict: Restrict,
    ) = facade.addNovelBookmark(novelId, restrict)

    override suspend fun removeNovelBookmark(novelId: Long) = facade.removeNovelBookmark(novelId)
}

private class NotificationRepositoryAdapter(
    private val facade: IllustiaRepository,
) : NotificationRepository {
    override suspend fun notifications() = facade.notifications()

    override suspend fun notificationViewMore(notificationId: Long) = facade.notificationViewMore(notificationId)

    override suspend fun nextNotificationPage(nextUrl: String) = facade.nextNotificationPage(nextUrl)
}

private class SettingsRepositoryAdapter(
    private val facade: IllustiaRepository,
) : SettingsRepository {
    override suspend fun readSettings() = facade.readSettings()

    override suspend fun readStartupSettings() = facade.readStartupSettings()

    override suspend fun saveSettings(
        settings: AppSettings,
        baseSettings: AppSettings?,
    ) = facade.saveSettings(settings, baseSettings)

    override suspend fun saveSettingsFromSync(settings: AppSettings) = facade.saveSettingsFromSync(settings)
}
