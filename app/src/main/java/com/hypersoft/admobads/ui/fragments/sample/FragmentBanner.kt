package com.hypersoft.admobads.ui.fragments.sample

import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Display
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.hypersoft.admobads.R
import com.hypersoft.admobads.databinding.FragmentBannerBinding
import com.hypersoft.admobads.adsconfig.AdmobBannerAds
import com.hypersoft.admobads.adsconfig.callbacks.BannerCallBack
import com.hypersoft.admobads.adsconfig.enums.CollapsiblePositionType
import com.hypersoft.admobads.helpers.firebase.RemoteConstants
import com.hypersoft.admobads.helpers.observers.SingleLiveEvent
import com.hypersoft.admobads.ui.fragments.base.BaseFragment

class FragmentBanner : BaseFragment<FragmentBannerBinding>(R.layout.fragment_banner) {

    /**
     * Don't use AdmobBannerAds in DI
     */
    private val admobBannerAds by lazy { AdmobBannerAds() }
    private val adsObserver = SingleLiveEvent<Boolean>()
    private var isCollapsibleOpen = false
    private var isBackPressed = false

    override fun onViewCreatedOneTime() {
       // loadAds()
        val adRequest = AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, Bundle().apply {
            putString("collapsible", "bottom")
        }).build()
        val adView = context?.let { AdView(it) }
        activity?.getAdSize()?.let { adView?.setAdSize(it) }
        adView?.adUnitId = "ca-app-pub-3940256099942544/2014213617"
        adView?.loadAd(adRequest)
        adView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                binding.adsBannerPlaceHolder.addView(adView)
            }

            override fun onAdClicked() {
                super.onAdClicked()

            }
        }
    }

    override fun onViewCreatedEverytime() {
        initObserver()
    }

    private fun initObserver(){
        adsObserver.observe(this){
            if (it){
                onBack()
            }
        }
    }

    override fun navIconBackPressed() {
        onBackPressed()
    }

    override fun onBackPressed() {
        if (isAdded){
            try {
                if (!isBackPressed){
                    isBackPressed = true
                    if (isCollapsibleOpen){
                        admobBannerAds.bannerOnDestroy()
                        binding.adsBannerPlaceHolder.removeAllViews()
                    }else{
                        onBack()
                    }
                }
            }catch (ex:Exception){
                isBackPressed = false
            }
        }
    }

    private fun onBack(){
        popFrom(R.id.fragmentBanner)
    }

    private fun loadAds(){
        admobBannerAds.loadBannerAds(
            activity,
            binding.adsBannerPlaceHolder,
            getResString(R.string.admob_banner_collapse_id),
            RemoteConstants.rcvBannerCollapsible,
            diComponent.sharedPreferenceUtils.isAppPurchased,
            diComponent.internetManager.isInternetConnected,
            CollapsiblePositionType.BOTTOM,
            object : BannerCallBack {
                override fun onAdFailedToLoad(adError: String) {}
                override fun onAdLoaded() {}
                override fun onAdImpression() {}
                override fun onPreloaded() {}
                override fun onAdClicked() {}
                override fun onAdSwipeGestureClicked() {}
                override fun onAdClosed() {
                    isCollapsibleOpen = false

                    if (isBackPressed){
                        adsObserver.value = true
                    }
                }

                override fun onAdOpened() {
                    isCollapsibleOpen = true
                }


            }
        )
    }

    override fun onPause() {
        admobBannerAds.bannerOnPause()
        super.onPause()
    }

    override fun onResume() {
        admobBannerAds.bannerOnResume()
        super.onResume()
    }

    override fun onDestroy() {
        admobBannerAds.bannerOnDestroy()
        super.onDestroy()
    }
}

fun Activity.getAdSize(): AdSize {

    val display: Display = windowManager.defaultDisplay
    val outMetrics = DisplayMetrics()
    display.getMetrics(outMetrics)
    val widthPixels = outMetrics.widthPixels.toFloat()
    val density = outMetrics.density
    val adWidth = (widthPixels / density).toInt()

    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
}