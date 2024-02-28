package com.grow.relationship.roots.admob

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.grow.relationship.roots.R

object RewardedAdUtils {
    private val TAG = "Rewarded_ad"
    private var rewardedAd: RewardedAd? = null
    var isAdLoaded = false

    fun loadRewardedVideoAd(context: Context?, adUnitId: String?) {
        if (!isAdLoaded) {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(context!!, adUnitId!!, adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Rewarded ad failed to load: " + loadAdError.message)
                    isAdLoaded = false
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded.")
                    rewardedAd = ad
                    isAdLoaded = true
                }
            })
        }
    }

    fun showRewardedVideoAd(activity: Activity, listener: OnAdShowedListener?) {
        if (rewardedAd != null && isAdLoaded) {
            rewardedAd!!.show(activity) {
                // Handle user reward here
                listener?.onAdDismissed()
            }
            rewardedAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed.")
                    listener?.onAdDismissed()
                    // Consider reloading the ad here if needed
                    isAdLoaded = false
                    Constants.SHOULD_NOT_SHOW_OPEN_APP_AD = false
                    loadRewardedVideoAd(activity, activity.getString(R.string.rewarded))
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    listener?.onAdDismissed()
                    isAdLoaded = false
                    Constants.SHOULD_NOT_SHOW_OPEN_APP_AD = false
                    loadRewardedVideoAd(activity, activity.getString(R.string.rewarded))
                }

                override fun onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    Log.d(TAG, "Ad showed fullscreen content.")
                    Constants.SHOULD_NOT_SHOW_OPEN_APP_AD = true
                }
            }
        } else {
            Log.d(TAG, "The rewarded ad wasn't loaded yet.")
            listener?.onAdDismissed()
        }
    }

    interface OnAdShowedListener {
        fun onAdDismissed()
    }
}