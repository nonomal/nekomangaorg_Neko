package eu.kanade.tachiyomi.ui.recents

import androidx.activity.compose.BackHandler
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import eu.kanade.tachiyomi.ui.base.controller.BaseComposeController
import eu.kanade.tachiyomi.ui.main.MainActivity
import eu.kanade.tachiyomi.ui.manga.MangaDetailController
import eu.kanade.tachiyomi.util.system.launchUI
import eu.kanade.tachiyomi.util.system.openInBrowser
import eu.kanade.tachiyomi.util.view.withFadeTransaction
import org.nekomanga.presentation.screens.FeedScreen

class FeedController : BaseComposeController<FeedPresenter>() {
    override val presenter = FeedPresenter()

    @Composable
    override fun ScreenContent() {
        val windowSizeClass = calculateWindowSizeClass(this.activity!!)

        BackHandler((this.activity as? MainActivity)?.shouldGoToStartingTab() == true) {
            (this.activity as? MainActivity)?.backCallback?.invoke()
        }

        FeedScreen(
            feedScreenState = presenter.feedScreenState.collectAsState(),
            loadNextPage = presenter::loadNextPage,
            onBackPress = router::handleBack,
            windowSizeClass = windowSizeClass,
            incognitoClick = presenter::toggleIncognitoMode,
            feedSettingActions = FeedSettingActions(
                groupHistoryClick = presenter::toggleGroupHistoryType,
            ),
            feedScreenActions = FeedScreenActions(
                mangaClick = ::openManga,
                switchViewType = presenter::switchViewType,
                deleteAllHistoryClick = presenter::deleteAllHistoryClick,
                deleteHistoryClick = presenter::deleteHistoryClick,
            ),
            settingsClick = { (this.activity as? MainActivity)?.showSettings() },
            statsClick = { (this.activity as? MainActivity)?.showStats() },
            aboutClick = { (this.activity as? MainActivity)?.showAbout() },
            helpClick = { (this.activity as? MainActivity)?.openInBrowser("https://tachiyomi.org/help/") },
        )
    }

    private fun openManga(mangaId: Long) {
        viewScope.launchUI {
            router.pushController(MangaDetailController(mangaId).withFadeTransaction())
        }
    }
}