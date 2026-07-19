package com.tenco

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.tenco.core.locale.LocaleManager
import com.tenco.core.prefs.AppPreferences
import com.tenco.feature.onboarding.LanguageScreen
import com.tenco.navigation.AppViewModel
import com.tenco.navigation.TencoNavHost
import com.tenco.ui.theme.TencoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val language = AppPreferences(newBase).language
        super.attachBaseContext(LocaleManager.wrap(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { TencoApp() }
    }
}

@Composable
private fun TencoApp() {
    TencoTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            val appViewModel: AppViewModel = hiltViewModel()
            val context = LocalContext.current
            var showSplash by remember { mutableStateOf(true) }
            androidx.compose.runtime.LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1300)
                showSplash = false
            }
            // `remember` (not rememberSaveable) so this re-initialises after a locale recreate.
            var showLanguage by remember { mutableStateOf(!appViewModel.isLanguageChosen) }

            if (showSplash) {
                com.tenco.feature.onboarding.SplashScreen()
            } else if (showLanguage) {
                LanguageScreen(
                    current = appViewModel.currentLanguage,
                    onChosen = { language ->
                        appViewModel.setLanguage(language)
                        // Recreate so attachBaseContext re-applies the locale app-wide.
                        // After recreate, isLanguageChosen == true, so this gate falls through
                        // to the NavHost (which is composed fresh, with no stale saved state).
                        context.findActivity()?.recreate()
                    },
                )
            } else {
                TencoNavHost(
                    appViewModel = appViewModel,
                    onChangeLanguage = { showLanguage = true },
                )
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
