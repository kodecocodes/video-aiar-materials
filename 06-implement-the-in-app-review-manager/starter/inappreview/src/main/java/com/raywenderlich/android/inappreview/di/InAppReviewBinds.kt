package com.raywenderlich.android.inappreview.di

import com.raywenderlich.android.inappreview.preferences.InAppReviewPreferences
import com.raywenderlich.android.inappreview.preferences.InAppReviewPreferencesImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

/**
 * Provides dependencies required for In App Review flow.
 * */
@Module
@InstallIn(ApplicationComponent::class)
abstract class InAppReviewBinds {

  /**
   * Provides Preferences wrapper.
   * */
  @Binds
  @Singleton
  abstract fun bindInAppReviewPreferences(
    inAppReviewPreferencesImpl: InAppReviewPreferencesImpl
  ): InAppReviewPreferences
}