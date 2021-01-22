/*
 * Copyright (c) 2021 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */package com.raywenderlich.android.inappreview

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.tasks.Task
import com.raywenderlich.android.inappreview.preferences.InAppReviewPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

class InAppReviewManagerImpl @Inject constructor(
  @ApplicationContext private val context: Context,
  private val reviewManager: ReviewManager,
  private val inAppReviewPreferences: InAppReviewPreferences
) : InAppReviewManager {

  companion object {
    private const val KEY_REVIEW = "reviewFlow"
  }

  private var reviewInfo: ReviewInfo? = null

  init {
    if (isEligibleForReview()) {
      reviewManager.requestReviewFlow().addOnCompleteListener {
        if (it.isComplete && it.isSuccessful) {
          this.reviewInfo = it.result
        }
      }
    }
  }

  override fun isEligibleForReview(): Boolean {
    return (!inAppReviewPreferences.hasUserRatedApp() &&
            !inAppReviewPreferences.hasUserChosenRateLater())
            || (inAppReviewPreferences.hasUserChosenRateLater() && enoughTimePassed())
  }

  private fun enoughTimePassed(): Boolean {
    val rateLaterTimestamp = inAppReviewPreferences.getRateLaterTime()

    return abs(rateLaterTimestamp - System.currentTimeMillis()) >=
            TimeUnit.DAYS.toMillis(14)
  }

  override fun startReview(activity: Activity) {
    if (reviewInfo != null) {
      reviewManager.launchReviewFlow(activity, reviewInfo).addOnCompleteListener { reviewFlow ->
        onReviewFlowLaunchCompleted(reviewFlow)
      }
    }
  }

  private fun onReviewFlowLaunchCompleted(reviewFlow: Task<Void>) {
    if (reviewFlow.isSuccessful) {
      logSuccess()
    }
  }

  private fun logSuccess() {
    if (BuildConfig.DEBUG) {
      Log.d(KEY_REVIEW, "Review Complete!")
    }
  }
}