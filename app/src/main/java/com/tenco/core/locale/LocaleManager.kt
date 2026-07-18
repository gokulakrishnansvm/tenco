package com.tenco.core.locale

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Applies a per-app language override by wrapping a base [Context] with a locale-specific
 * configuration. Framework-only (no appcompat). Call [wrap] from Activity.attachBaseContext,
 * and recreate the activity when the language changes.
 */
object LocaleManager {
    fun wrap(base: Context, language: AppLanguage): Context {
        val locale = Locale(language.tag)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return base.createConfigurationContext(config)
    }
}
