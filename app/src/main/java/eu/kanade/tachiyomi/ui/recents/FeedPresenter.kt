package eu.kanade.tachiyomi.ui.recents

import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.ui.base.presenter.BaseCoroutinePresenter
import eu.kanade.tachiyomi.util.system.SideNavMode
import eu.kanade.tachiyomi.util.system.launchIO
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nekomanga.core.preferences.toggle
import org.nekomanga.core.security.SecurityPreferences
import org.nekomanga.domain.details.MangaDetailsPreferences
import org.nekomanga.domain.library.LibraryPreferences
import org.nekomanga.util.paging.DefaultPaginator
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class FeedPresenter(
    val preferences: PreferencesHelper = Injekt.get(),
    val libraryPreferences: LibraryPreferences = Injekt.get(),
    val securityPreferences: SecurityPreferences = Injekt.get(),
    val mangaDetailsPreferences: MangaDetailsPreferences = Injekt.get(),
    private val feedRepository: FeedRepository = Injekt.get(),

    ) : BaseCoroutinePresenter<FeedController>() {

    private val _feedScreenState = MutableStateFlow(
        FeedScreenState(
            feedScreenType = preferences.feedViewType().get(),
            outlineCovers = libraryPreferences.outlineOnCovers().get(),
            incognitoMode = securityPreferences.incognitoMode().get(),
            groupChaptersUpdates = preferences.groupChaptersUpdates().get(),
            historyGrouping = preferences.historyChapterGrouping().get(),
            hideChapterTitles = mangaDetailsPreferences.hideChapterTitlesByDefault().get(),
        ),
    )
    val feedScreenState: StateFlow<FeedScreenState> = _feedScreenState.asStateFlow()

    private val paginator = DefaultPaginator(
        initialKey = _feedScreenState.value.offset,
        onLoadUpdated = { },
        onRequest = {
            feedRepository.getPage(_feedScreenState.value.offset, _feedScreenState.value.feedScreenType, _feedScreenState.value.historyGrouping)
        },
        getNextKey = {
            _feedScreenState.value.offset + ENDLESS_LIMIT
        },
        onError = {
            //TODO
        },
        onSuccess = { hasNextPage, items, newKey ->
            _feedScreenState.update { state ->
                state.copy(
                    allFeedManga = (state.allFeedManga + items).toImmutableList(),
                    offset = newKey,
                    hasMoreResults = hasNextPage,
                )
            }

        },
    )

    override fun onCreate() {
        super.onCreate()

        if (_feedScreenState.value.initialLoad) {
            _feedScreenState.update { state ->
                state.copy(initialLoad = false)
            }
            presenterScope.launchIO { loadNextPage() }
        }

        presenterScope.launch {
            _feedScreenState.update {
                it.copy(sideNavMode = SideNavMode.findByPrefValue(preferences.sideNavMode().get()))
            }
        }


        presenterScope.launch {
            securityPreferences.incognitoMode().changes().collectLatest {
                _feedScreenState.update { state ->
                    state.copy(incognitoMode = it)
                }
            }
        }
        presenterScope.launch {
            preferences.groupChaptersUpdates().changes().collectLatest {
                _feedScreenState.update { state ->
                    state.copy(groupChaptersUpdates = it)
                }
            }
        }

        presenterScope.launch {
            preferences.historyChapterGrouping().changes().collectLatest {
                _feedScreenState.update { state ->
                    state.copy(historyGrouping = it, offset = 0, allFeedManga = persistentListOf())
                }
                paginator.reset()
                loadNextPage()
            }
        }

        presenterScope.launch {
            preferences.feedViewType().changes().collectLatest {
                _feedScreenState.update { state ->
                    state.copy(feedScreenType = it, offset = 0, allFeedManga = persistentListOf())
                }
                paginator.reset()
                loadNextPage()
            }
        }
    }

    fun loadNextPage() {
        presenterScope.launchIO {
            paginator.loadNextItems()
        }
    }

    fun switchViewType(feedScreenType: FeedScreenType) {
        presenterScope.launch {
            preferences.feedViewType().set(feedScreenType)
        }
    }

    fun toggleGroupHistoryType(historyGrouping: FeedHistoryGroup) {
        presenterScope.launch {
            preferences.historyChapterGrouping().set(historyGrouping)

        }
    }

    fun toggleIncognitoMode() {
        presenterScope.launch {
            securityPreferences.incognitoMode().toggle()
        }
    }

    fun deleteAllHistoryClick() {
    }

    fun deleteHistoryClick() {
    }


    companion object {
        const val ENDLESS_LIMIT = 50
    }
}