package com.ados.everybodysingerrematch

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fsn.cauly.CaulyAdInfo
import com.fsn.cauly.CaulyAdInfoBuilder
import com.fsn.cauly.CaulyInterstitialAd
import com.fsn.cauly.CaulyInterstitialAdListener
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.firestore.FirebaseFirestore
import com.ados.everybodysingerrematch.model.PreferencesDTO
import com.ados.everybodysingerrematch.model.UpdateDTO
import org.jetbrains.anko.toast

class SplashActivity : AppCompatActivity() {

    var firestore : FirebaseFirestore? = null
    var mainIntent : Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainIntent = Intent(this, MainActivity::class.java)

        firestore = FirebaseFirestore.getInstance()

        // 앱 최초 설정
        /*val pre = PreferencesDTO()
        pre.IntervalTime = 5L
        pre.maxTicketCount = 10
        pre.ticketChargeCount = 10
        pre.runHotTime = false
        pre.rewardName = "광고를 보고 투표권을 2개 받으세요"
        pre.rewardName2 = "광고를 보고 뽑기권을 1개 받으세요"
        pre.rewardBonus = 2
        pre.rewardBonus2 = 1
        pre.rewardCount = 80
        pre.rewardIntervalTime = 2
        pre.questCount = 5
        pre.lottoMaxCount = 10
        pre.ticketSaveMaxCount = 1000
        firestore?.collection("preferences")?.document("preferences")?.set(pre)

        val update = UpdateDTO()
        update.version = "1.1"
        update.visibility = false
        update.essential = "N"
        update.updateUrl = ""
        update.maintainance = false
        update.maintainanceTitle = ""
        update.maintainanceDesc = ""
        firestore?.collection("preferences")?.document("update")?.set(update)*/



        // 광고 종류 획득
        firestore?.collection("preferences")?.document("ad_policy")?.get()?.addOnCompleteListener { task ->
            if(task.isSuccessful){
                var ad_interstitial = task.result!!["ad_interstitial"]
                callInterstitial(ad_interstitial as String)
            }
        }


        //SystemClock.sleep(300)


    }

    fun callInterstitial(interstitial : String) {
        when (interstitial) {
            getString(R.string.adtype_admob) -> {
                interstitialAdmob(true)
            }
            getString(R.string.adtype_cauly) -> {
                interstitialCauly(true)
            }
            else -> {

            }
        }
    }

    fun interstitialAdmob(isFirst : Boolean) {
        // 애드몹 - 전면
        var InterstitialAd = InterstitialAd(this)
        InterstitialAd.adUnitId = getString(R.string.admob_Interstitial_ad_unit_id) // 관리자모드
        InterstitialAd.loadAd(AdRequest.Builder().build())

        InterstitialAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                if (InterstitialAd.isLoaded) {
                    InterstitialAd.show()
                } else {
                    // 광고 호출 실패
                    if (isFirst) {
                        interstitialCauly(false)
                    } else {
                        startActivity(mainIntent)
                        finish()
                    }
                }
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                // Code to be executed when an ad request fails.
                if (isFirst) {
                    interstitialCauly(false)
                } else {
                    startActivity(mainIntent)
                    finish()
                }
            }

            override fun onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {
                startActivity(mainIntent)
                finish()
            }
        }
    }

    fun interstitialCauly(isFirst : Boolean) {
        var adInfo: CaulyAdInfo
        adInfo = CaulyAdInfoBuilder("OgwgVd8s").build()
        var interstial = CaulyInterstitialAd()
        interstial.setAdInfo(adInfo)

        val adCallback = object : CaulyInterstitialAdListener {
            override fun onReceiveInterstitialAd(p0: CaulyInterstitialAd?, p1: Boolean) {
                p0?.show()
            }

            override fun onFailedToReceiveInterstitialAd(p0: CaulyInterstitialAd?, p1: Int, p2: String?) {
                if (isFirst) {
                    interstitialAdmob(false)
                } else {
                    startActivity(mainIntent)
                    finish()
                }
            }

            override fun onClosedInterstitialAd(p0: CaulyInterstitialAd?) {
                startActivity(mainIntent)
                finish()
            }

            override fun onLeaveInterstitialAd(p0: CaulyInterstitialAd?) {

            }

        }

        interstial.setInterstialAdListener(adCallback)
        interstial.requestInterstitialAd(this)
    }
}