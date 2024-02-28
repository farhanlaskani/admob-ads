package com.grow.relationship.roots.admob

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object InterAdUtils {
    val TAG = "Interstitial_ad"
    var isInterstitialShown = false
    private var adFailedAttempts = 3

    private var admobInterAd: InterstitialAd? = null
    private var adFailedCounter = 0
    private var isAdLoaded = false
    private fun isAdLoaded(): Boolean {
        Log.d(TAG, "isAdLoaded: $isAdLoaded $admobInterAd")
        return isAdLoaded && admobInterAd != null
    }

    fun loadInterstitialAd(context: Context, adInterId: String, listener: AdLoadListener?) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, adInterId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                isAdLoaded = false
                handleErrorOnAdLoad(context, adInterId, listener)
                Log.d(TAG, "onAdFailedToLoad: $adError")
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                isAdLoaded = true
                handleSuccessfulAdLoad(ad, listener)
                Log.d(TAG, "onAdLoaded: is listener is null " + (listener == null))
            }
        })
    }

    private fun handleErrorOnAdLoad(
        context: Context,
        adInterId: String,
        listener: AdLoadListener?
    ) {
        Log.e(TAG, "onAdFailedToLoad")
        resetAdStatus()
        adFailedCounter++
        if (adFailedCounter < adFailedAttempts) {
            loadInterstitialAd(context, adInterId, listener)
        }
        listener?.onAdLoadFailed()
    }

    private fun handleSuccessfulAdLoad(ad: InterstitialAd, listener: AdLoadListener?) {
        admobInterAd = ad
        isAdLoaded = true
        isInterstitialShown = false
        Log.e(TAG, "Ad Loaded Successfully")
        listener?.onAdLoaded()
    }

    private fun resetAdStatus() {
        admobInterAd = null
        isAdLoaded = false
        isInterstitialShown = false
        Constants.SHOULD_NOT_SHOW_OPEN_APP_AD = false
    }

    fun showInterstitialAd(
        activity: Activity,
        adInterId: String,
        isClosedAd: () -> Unit,

    ) {
        if (isAdLoaded()) {
            Constants.SHOULD_NOT_SHOW_OPEN_APP_AD = true
            admobInterAd!!.show(activity)
        } else {
            isClosedAd()

        }
        setupAdCallbacks(activity, adInterId, isClosedAd)
    }

    private fun setupAdCallbacks(
        activity: Activity,
        adInterId: String,
        isClosedAd: () -> Unit,

    ) {
        admobInterAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.e(TAG, "onAdDismissedFullScreenContent")
                resetAdStatus()
                loadInterstitialAd(activity, adInterId, object : AdLoadListener {
                    override fun onAdLoaded() {
                        isAdLoaded = true
                    }

                    override fun onAdLoadFailed() {
                        isAdLoaded = false

                    }
                })

                isClosedAd()


            }


            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "onAdFailedToShowFullScreenContent")
                resetAdStatus()
                loadInterstitialAd(activity, adInterId, object : AdLoadListener {
                    override fun onAdLoaded() {
                        isAdLoaded = true
                    }
                    override fun onAdLoadFailed() {
                        isAdLoaded = false

                    }
                })
                isClosedAd()

            }

            override fun onAdImpression() {
                isInterstitialShown = true
            }

            override fun onAdShowedFullScreenContent() {
                isInterstitialShown = true
            }
        }
    }

    interface AdLoadListener {
        fun onAdLoaded()
        fun onAdLoadFailed()
    }


}