package com.grow.relationship.roots.admob

import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

object BannerAdUtils {
    fun showAdmobBanner(
        context: Context,
        adId: String?,
        bannerLayout: FrameLayout,
        adListener: (Boolean) -> Unit
    ) {
        val displayMetrics = context.resources.displayMetrics
        val widthPixels = displayMetrics.widthPixels
        val density = displayMetrics.density
        val adWidth = (widthPixels / density).toInt()
        val adaptiveAdSize =
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
        val adView = AdView(context)
        adView.adUnitId = adId!!
        adView.setAdSize(adaptiveAdSize)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                adListener(true)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                super.onAdFailedToLoad(adError)
                Log.e("Admob_Banner", "onAdFailedToLoad - $adError")
                adListener(
                    false
                )
            }
        }

        bannerLayout.addView(adView)
        adView.loadAd(AdRequest.Builder().build())
    }
}