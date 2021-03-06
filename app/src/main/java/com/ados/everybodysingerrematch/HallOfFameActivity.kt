package com.ados.everybodysingerrematch

import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Dimension
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.everybodysingerrematch.dialog.DonationCertificateDialog
import com.ados.everybodysingerrematch.dialog.LoadingDialog
import com.ados.everybodysingerrematch.model.*
import com.ados.everybodysingerrematch.page.OnCheeringItemClickListener
import com.ados.everybodysingerrematch.page.OnRankItemClickListener
import com.ados.everybodysingerrematch.page.RecyclerViewAdapterCheering
import com.ados.everybodysingerrematch.page.RecyclerViewAdapterRank
import com.bumptech.glide.Glide
import com.fsn.cauly.*
import com.google.android.gms.ads.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_hall_of_fame.*
import kotlinx.android.synthetic.main.activity_hall_of_fame.adView_kakao
import kotlinx.android.synthetic.main.activity_hall_of_fame.img_rank_background
import kotlinx.android.synthetic.main.activity_hall_of_fame.img_season_logo
import kotlinx.android.synthetic.main.activity_hall_of_fame.layout_adview
import kotlinx.android.synthetic.main.activity_hall_of_fame.profile_rank_no1
import kotlinx.android.synthetic.main.activity_hall_of_fame.profile_rank_no2
import kotlinx.android.synthetic.main.activity_hall_of_fame.profile_rank_no3
import kotlinx.android.synthetic.main.activity_hall_of_fame.recyclerview_rank
import kotlinx.android.synthetic.main.donation_certificate_dialog.*
import kotlinx.android.synthetic.main.fragment_fragment_page_rank.*
import kotlinx.android.synthetic.main.profile_item.view.*
import kotlinx.android.synthetic.main.profile_item.view.img_profile
import kotlinx.android.synthetic.main.profile_item.view.text_count
import kotlinx.android.synthetic.main.profile_item.view.text_name
import org.jetbrains.anko.image
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList
import kotlin.concurrent.timer

class HallOfFameActivity : AppCompatActivity(), OnRankItemClickListener, OnCheeringItemClickListener {

    enum class DISPLAY_TYPE {
        VOTE, CHEERING, ONE_MILLION, TWO_MILLION, THREE_MILLION, FOUR_MILLION, FIVE_MILLION, SIX_MILLION, DONATION
    }

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    var adType: String? = null
    lateinit var mAdView : AdView
    lateinit var mInterstitialAd : InterstitialAd
    lateinit var mAdViewCauly : CaulyAdView

    var firestore : FirebaseFirestore? = null
    lateinit var recyclerView : RecyclerView
    var loadingDialog : LoadingDialog? = null
    private var seasonDTO : SeasonDTO? = null
    private var selectedSeason = "season_1"
    private var isFinished = false

    private var displayTypeIndex = 0
    private val displayTypes : List<DISPLAY_TYPE> = listOf(DISPLAY_TYPE.VOTE, DISPLAY_TYPE.CHEERING, DISPLAY_TYPE.DONATION)

    private var peopleTop3_Vote : ArrayList<RankDTO> = arrayListOf()
    private var peopleOther_Vote : ArrayList<RankDTO> = arrayListOf()
    private var peopleOther_Donation : ArrayList<RankDTO> = arrayListOf()

    private var peopleTop3_Cheering : ArrayList<RankDTO> = arrayListOf()
    private var peopleOther_Cheering : ArrayList<BoardDTO> = arrayListOf()

    /*private var peopleTop3_OneMillon : ArrayList<RankDTO> = arrayListOf()
    private var peopleOther_OneMillon : ArrayList<RankDTO> = arrayListOf()

    private var peopleTop3_TwoMillon : ArrayList<RankDTO> = arrayListOf()
    private var peopleOther_TwoMillon : ArrayList<RankDTO> = arrayListOf()

    private var peopleTop3_ThreeMillon : ArrayList<RankDTO> = arrayListOf()
    private var peopleOther_ThreeMillon : ArrayList<RankDTO> = arrayListOf()

    private var peopleTop3_FourMillon : ArrayList<RankDTO> = arrayListOf()
    private var peopleOther_FourMillon : ArrayList<RankDTO> = arrayListOf()

    private var peopleTop3_FiveMillon : ArrayList<RankDTO> = arrayListOf()
    private var peopleOther_FiveMillon : ArrayList<RankDTO> = arrayListOf()

    private var peopleTop3_SixMillon : ArrayList<RankDTO> = arrayListOf()
    private var peopleOther_SixMillon : ArrayList<RankDTO> = arrayListOf()*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hall_of_fame)

        recyclerView = recyclerview_rank
        recyclerView.layoutManager = LinearLayoutManager(this)

        firestore = FirebaseFirestore.getInstance()
        firestore?.collection("preferences")?.document("season")?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            seasonDTO = documentSnapshot?.toObject(SeasonDTO::class.java)

            // ?????? ?????? ??????
            //seasonDTO?.seasonNum = 2

            selectedSeason = "season_${seasonDTO?.seasonNum!!.minus(1)}"

            // ??????2
            if (seasonDTO?.seasonNum!! == 2) {
                text_other_season.visibility = View.GONE
            }

            loadData()
        }

        // AD
        mAdView = findViewById(R.id.adView_admob)
        mAdViewCauly = findViewById(R.id.xmladview)

        InitAd()

        println("?????? ??????")
        //showAd()

        //loadData()

        // Top 3 ?????? ??????
        profile_rank_no1.visibility = View.GONE
        profile_rank_no2.visibility = View.GONE
        profile_rank_no3.visibility = View.GONE

        //profile_rank_no1.img_rank.setImageResource(R.drawable.crown_gold)
        //profile_rank_no2.img_rank.setImageResource(R.drawable.crown_silver)
        //profile_rank_no3.img_rank.setImageResource(R.drawable.crown_bronze)
        Glide.with(profile_rank_no1.img_rank.context)
            .asBitmap()
            .load(R.drawable.crown_gold) ///feed in path of the image
            .fitCenter()
            .into(profile_rank_no1.img_rank)
        Glide.with(profile_rank_no2.img_rank.context)
            .asBitmap()
            .load(R.drawable.crown_silver) ///feed in path of the image
            .fitCenter()
            .into(profile_rank_no2.img_rank)
        Glide.with(profile_rank_no3.img_rank.context)
            .asBitmap()
            .load(R.drawable.crown_bronze) ///feed in path of the image
            .fitCenter()
            .into(profile_rank_no3.img_rank)

        Glide.with(img_left.context)
            .asBitmap()
            .load(R.drawable.halloffame_title) ///feed in path of the image
            .fitCenter()
            .into(img_title)
        Glide.with(img_left.context)
            .asBitmap()
            .load(R.drawable.left_arrow) ///feed in path of the image
            .fitCenter()
            .into(img_left)
        Glide.with(img_right.context)
            .asBitmap()
            .load(R.drawable.right_arrow) ///feed in path of the image
            .fitCenter()
            .into(img_right)


        profile_rank_no1.text_rank.text = "??????  1???"
        profile_rank_no1.text_rank.setTextSize(Dimension.SP, 10.toFloat())
        profile_rank_no1.text_name.setTextSize(Dimension.SP, 10.toFloat())
        profile_rank_no1.text_count.setTextSize(Dimension.SP, 10.toFloat())

        profile_rank_no2.text_rank.text = "??????  2???"
        profile_rank_no2.text_rank.setTextSize(Dimension.SP, 9.toFloat())
        profile_rank_no2.text_name.setTextSize(Dimension.SP, 9.toFloat())
        profile_rank_no2.text_count.setTextSize(Dimension.SP, 9.toFloat())

        profile_rank_no3.text_rank.text = "??????  3???"
        profile_rank_no3.text_rank.setTextSize(Dimension.SP, 8.toFloat())
        profile_rank_no3.text_name.setTextSize(Dimension.SP, 8.toFloat())
        profile_rank_no3.text_count.setTextSize(Dimension.SP, 8.toFloat())

        /*loading()
        timer(period = 100)
        {
            if (peopleTop3_Vote.size > 0) {
                cancel()
                //display()
                runOnUiThread {
                    display()
                    loadingEnd()
                }
            }
        }*/


        img_left.setOnClickListener {
            if (displayTypeIndex == 0)
                displayTypeIndex = displayTypes.size - 1
            else
                displayTypeIndex--

            var animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.horizon_left)
            main_layout.startAnimation(animation)

            display()
        }

        img_right.setOnClickListener {
            if (displayTypeIndex == (displayTypes.size - 1))
                displayTypeIndex = 0
            else
                displayTypeIndex++

            var animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.horizon_right)
            main_layout.startAnimation(animation)

            display()
        }

        profile_rank_no1.setOnClickListener {
            when(displayTypes[displayTypeIndex]) {
                DISPLAY_TYPE.DONATION, DISPLAY_TYPE.VOTE -> {
                    callDonationCertificateDialog(peopleTop3_Vote[0], 1)
                }
            }
        }
        profile_rank_no2.setOnClickListener {
            when(displayTypes[displayTypeIndex]) {
                DISPLAY_TYPE.DONATION, DISPLAY_TYPE.VOTE -> {
                    callDonationCertificateDialog(peopleTop3_Vote[1], 2)
                }
            }
        }
        profile_rank_no3.setOnClickListener {
            when(displayTypes[displayTypeIndex]) {
                DISPLAY_TYPE.DONATION, DISPLAY_TYPE.VOTE -> {
                    callDonationCertificateDialog(peopleTop3_Vote[2], 3)
                }
            }
        }

        text_other_season.paintFlags = text_other_season.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        text_other_season.setOnClickListener {
            var pop = PopupMenu(this, text_other_season)
            //menuInflater.inflate(R.menu.halloffame_menu, pop.menu)
            pop.menu.add(Menu.NONE, R.id.Item1, Menu.NONE, "??????1 ????????? ??????")
            pop.menu.add(Menu.NONE, R.id.Item2, Menu.NONE, "??????2 ????????? ??????")

            if (seasonDTO != null) {
                // ??????4 ?????? ????????? ?????? 3??? ?????? ?????????
                if (seasonDTO?.seasonNum!! >= 4) {
                    pop.menu.add(Menu.NONE, R.id.Item3, Menu.NONE, "??????3 ????????? ??????")
                }
                if (seasonDTO?.seasonNum!! >= 5) {
                    pop.menu.add(Menu.NONE, R.id.Item4, Menu.NONE, "??????4 ????????? ??????")
                }
                if (seasonDTO?.seasonNum!! >= 6) {
                    pop.menu.add(Menu.NONE, R.id.Item5, Menu.NONE, "??????5 ????????? ??????")
                }
                if (seasonDTO?.seasonNum!! >= 7) {
                    pop.menu.add(Menu.NONE, R.id.Item6, Menu.NONE, "??????6 ????????? ??????")
                }
                if (seasonDTO?.seasonNum!! >= 8) {
                    pop.menu.add(Menu.NONE, R.id.Item7, Menu.NONE, "??????7 ????????? ??????")
                }
                if (seasonDTO?.seasonNum!! >= 9) {
                    pop.menu.add(Menu.NONE, R.id.Item8, Menu.NONE, "??????8 ????????? ??????")
                }
                if (seasonDTO?.seasonNum!! >= 10) {
                    pop.menu.add(Menu.NONE, R.id.Item9, Menu.NONE, "??????9 ????????? ??????")
                }
            }


            // 1. ???????????? ??????
            var listener = PopupListener()
            pop.setOnMenuItemClickListener(listener)

            // 2. ??????????????? ??????
            pop.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.Item1 -> {
                        if (selectedSeason != "season_1") {
                            selectedSeason = "season_1"
                            showAd()
                            loadData()
                        }
                    }
                    R.id.Item2 -> {
                        if (selectedSeason != "season_2") {
                            selectedSeason = "season_2"
                            showAd()
                            loadData()
                        }
                    }
                    R.id.Item3 -> {
                        if (selectedSeason != "season_3") {
                            selectedSeason = "season_3"
                            showAd()
                            loadData()
                        }
                    }
                    R.id.Item4 -> {
                        if (selectedSeason != "season_4") {
                            selectedSeason = "season_4"
                            loadData()
                        }
                    }
                    R.id.Item5 -> {
                        if (selectedSeason != "season_5") {
                            selectedSeason = "season_5"
                            loadData()
                        }
                    }
                    // ?????? ?????? ??????
                    /*
                    R.id.Item6 -> {
                        if (selectedSeason != "season_6") {
                            selectedSeason = "season_6"
                            loadData()
                        }
                    }
                    R.id.Item7 -> {
                        if (selectedSeason != "season_7") {
                            selectedSeason = "season_7"
                            loadData()
                        }
                    }
                    R.id.Item8 -> {
                        if (selectedSeason != "season_8") {
                            selectedSeason = "season_8"
                            loadData()
                        }
                    }
                    R.id.Item9 -> {
                        if (selectedSeason != "season_9") {
                            selectedSeason = "season_9"
                            loadData()
                        }
                    }*/
                }
                false
            }
            pop.show()
        }


        // ?????? ??????
        // ?????? ?????? ??????
        /*firestore?.collection("news")?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(NewsDTO::class.java)!!
                firestore?.collection("season_result")?.document("season_1")?.collection("news")?.document()?.set(person)
            }
        }
            ?.addOnFailureListener { exception ->

            }*/
        // ?????? ??? ??????
        /*firestore?.collection("people")?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                firestore?.collection("season_result")?.document("season_1")?.collection("vote")?.document(person.docname!!)?.set(person)
            }
        }
            ?.addOnFailureListener { exception ->

            }*/

        // ????????? ??????
        /*firestore?.collection("people_cheering")?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                firestore?.collection("season_result")?.document("season_1")?.collection("cheering")?.document(person.docname!!)?.set(person)
            }
        }
            ?.addOnFailureListener { exception ->

            }*/

        // 100??? ?????? ??????
        /*firestore?.collection("people")?.whereGreaterThan("count", 1000000)?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                firestore?.collection("season_result")?.document("season_5")?.collection("one_million")?.document(person.docname!!)?.set(person)
            }
        }
            ?.addOnFailureListener { exception ->

            }*/

       /*firestore?.collection("season_result")?.document("season_5")?.collection("vote")?.document("no00033")?.collection("donationNews")?.orderBy("order", Query.Direction.ASCENDING)?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var news = document.toObject(DonationNewsDTO::class.java)!!

                firestore?.collection("season_result")?.document(selectedSeason)?.collection("vote")?.document("no00033")?.collection("donationNews")?.document()?.set(news) // ?????????
                firestore?.collection("season_result")?.document(selectedSeason)?.collection("vote")?.document("no00035")?.collection("donationNews")?.document()?.set(news) // ?????????
                firestore?.collection("season_result")?.document(selectedSeason)?.collection("vote")?.document("no00026")?.collection("donationNews")?.document()?.set(news) // ??????
                firestore?.collection("season_result")?.document(selectedSeason)?.collection("vote")?.document("no00037")?.collection("donationNews")?.document()?.set(news) // ?????????
                firestore?.collection("season_result")?.document(selectedSeason)?.collection("vote")?.document("no00036")?.collection("donationNews")?.document()?.set(news) // ?????????
                firestore?.collection("season_result")?.document(selectedSeason)?.collection("vote")?.document("no00012")?.collection("donationNews")?.document()?.set(news) // ?????????
                firestore?.collection("season_result")?.document(selectedSeason)?.collection("vote")?.document("no00023")?.collection("donationNews")?.document()?.set(news) // ?????????
                firestore?.collection("season_result")?.document(selectedSeason)?.collection("vote")?.document("no00021")?.collection("donationNews")?.document()?.set(news) // ??????
            }

        }*/
    }

    inner class PopupListener : PopupMenu.OnMenuItemClickListener {
        override fun onMenuItemClick(p0: MenuItem?): Boolean {
            when (p0?.itemId) {
                R.id.Item1 ->
                    text_other_season.text = "menu1"
                R.id.Item2 ->
                    text_other_season.text = "menu1"
                R.id.Item3 ->
                    text_other_season.text = "menu1"
                R.id.Item4 ->
                    text_other_season.text = "menu1"
                R.id.Item5 ->
                    text_other_season.text = "menu1"
                R.id.Item6 ->
                    text_other_season.text = "menu1"
                R.id.Item7 ->
                    text_other_season.text = "menu1"
                R.id.Item8 ->
                    text_other_season.text = "menu1"
                R.id.Item9 ->
                    text_other_season.text = "menu1"
            }
            return false
        }

    }

    override fun onBackPressed() {
        isFinished = true
        showAd()

        //super.onBackPressed()
    }

    fun loadData() {
        var rank_background_img = R.drawable.spotlight_s1_main
        var season_logo_img = R.drawable.season1_logo
        when (selectedSeason) {
            "season_1" -> {
                rank_background_img = R.drawable.spotlight_s1_main
                season_logo_img = R.drawable.season1_logo
            }
            "season_2" -> {
                rank_background_img = R.drawable.spotlight_s2_main
                season_logo_img = R.drawable.season2_logo
            }
            "season_3" -> {
                rank_background_img = R.drawable.spotlight_s3_main
                season_logo_img = R.drawable.season3_logo
            }
            "season_4" -> {
                rank_background_img = R.drawable.spotlight_s4_main
                season_logo_img = R.drawable.season4_logo
            }
            "season_5" -> {
                rank_background_img = R.drawable.spotlight_s5_main
                season_logo_img = R.drawable.season5_logo
            }
            "season_6" -> {
                rank_background_img = R.drawable.spotlight_s6_main
                season_logo_img = R.drawable.season6_logo
            }
            // ?????? ?????? ??????
            /*
            "season_7" -> {
                rank_background_img = R.drawable.spotlight_s7_main
                season_logo_img = R.drawable.season7_logo
            }
            "season_8" -> {
                rank_background_img = R.drawable.spotlight_s8_main
                season_logo_img = R.drawable.season8_logo
            }
            "season_9" -> {
                rank_background_img = R.drawable.spotlight_s9_main
                season_logo_img = R.drawable.season9_logo
            }*/
        }
        Glide.with(img_rank_background.context)
            .asBitmap()
            .load(rank_background_img) ///feed in path of the image
            .fitCenter()
            .into(img_rank_background)
        Glide.with(img_season_logo.context)
            .asBitmap()
            .load(season_logo_img) ///feed in path of the image
            .fitCenter()
            .into(img_season_logo)

        peopleTop3_Vote.clear()
        peopleOther_Vote.clear()
        peopleOther_Donation.clear()
        peopleTop3_Cheering.clear()
        peopleOther_Cheering.clear()
        /*peopleTop3_OneMillon.clear()
        peopleOther_OneMillon.clear()
        peopleTop3_TwoMillon.clear()
        peopleOther_TwoMillon.clear()
        peopleTop3_ThreeMillon.clear()
        peopleOther_ThreeMillon.clear()
        peopleTop3_FourMillon.clear()
        peopleOther_FourMillon.clear()
        peopleTop3_FiveMillon.clear()
        peopleOther_FiveMillon.clear()
         peopleTop3_SixMillon.clear()
         peopleOther_SixMillon.clear()*/

        loadVote()
        loadCheering()
        /*loadVoteOver("one_million", peopleTop3_OneMillon, peopleOther_OneMillon)
        loadVoteOver("two_million", peopleTop3_TwoMillon, peopleOther_TwoMillon)
        loadVoteOver("three_million", peopleTop3_ThreeMillon, peopleOther_ThreeMillon)
        loadVoteOver("four_million", peopleTop3_FourMillon, peopleOther_FourMillon)
        loadVoteOver("five_million", peopleTop3_FiveMillon, peopleOther_FiveMillon)
        loadVoteOver("six_million", peopleTop3_SixMillon, peopleOther_SixMillon)*/

        loading()
        timer(period = 100)
        {
            if (peopleTop3_Vote.size > 0) {
                cancel()
                //display()
                runOnUiThread {
                    display()
                    loadingEnd()
                }
            }
        }
    }

    // ?????? ?????? ??????
    fun loadVote() {
        var index : Int = 1
        firestore?.collection("season_result")?.document(selectedSeason)?.collection("vote")?.orderBy("count", Query.Direction.DESCENDING)?.get()?.addOnSuccessListener { result ->
            index = 1
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                var person2 = document.toObject(RankDTO::class.java)!!
                if (index < 4) { // Top 3 ??????
                    peopleTop3_Vote.add(person)
                }
                else { // 4??? ?????? ??????
                    if (index < 8) // Top 7 ??????
                        peopleOther_Vote.add(person)
                    if (person2.count!! >= 1000000) {
                        person2.subTitle = "${decimalFormat.format(person2.count?.div(1000000)?.times(10))}?????? ??????"
                        peopleOther_Donation.add(person2)
                    }
                }
                index++
            }
        }
            ?.addOnFailureListener { exception ->

            }
    }

    // ?????? ?????? ??????
    fun loadCheering() {
        var index : Int = 1
        firestore?.collection("season_result")?.document(selectedSeason)?.collection("cheering")?.orderBy("cheeringCountTotal", Query.Direction.DESCENDING)?.limit(7)?.get()?.addOnSuccessListener { result ->
            index = 1
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                if (index < 4) { // Top 3 ??????
                    peopleTop3_Cheering.add(person)
                }

                var board = BoardDTO()
                board.image = person.image
                board.title = "${index}??? ${person.name}"
                board.content = "?????? ?????? : ${decimalFormat.format(person.cheeringCountTotal)}"
                board.name = "????????? : ${decimalFormat.format(person.cheeringCount)}"
                board.likeCount = person.likeCount
                board.dislikeCount = person.dislikeCount
                peopleOther_Cheering.add(board)

                index++
            }
        }
            ?.addOnFailureListener { exception ->

            }
    }

    // ????????? ?????? ??????
    fun loadVoteOver(collactionName : String, peopleTop3 : ArrayList<RankDTO>, peopleOther : ArrayList<RankDTO>) {
        var index : Int = 1
        println("?????? ??????")
        firestore?.collection("season_result")?.document(selectedSeason)?.collection(collactionName)?.orderBy("updateDate", Query.Direction.ASCENDING)?.get()?.addOnSuccessListener { result ->
            peopleTop3.clear()
            peopleOther.clear()

            println("?????? ??????")
            index = 1
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!

                if (index < 4) { // Top 3 ??????
                    peopleTop3.add(person)
                }
                else { // 4??? ?????? ??????
                    peopleOther.add(person)
                }
                index++
            }
        }
            ?.addOnFailureListener { exception ->

            }
    }

    fun ShowVoteTop3() {
        var imageID : Int = 0

        if (peopleTop3_Vote.size > 0 && profile_rank_no1 != null) {
            profile_rank_no1.visibility = View.VISIBLE
            imageID = resources?.getIdentifier(peopleTop3_Vote[0].image, "drawable", packageName)!!
            //profile_rank_no1.img_profile.setImageResource(imageID)
            Glide.with(profile_rank_no1.img_profile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .fitCenter()
                .into(profile_rank_no1.img_profile)
            profile_rank_no1.text_name.text = peopleTop3_Vote[0].name
            profile_rank_no1.text_count.text = "${decimalFormat.format(peopleTop3_Vote[0].count)}???"
        }
        if (peopleTop3_Vote.size > 1 && profile_rank_no2 != null) {
            profile_rank_no2.visibility = View.VISIBLE
            imageID = resources?.getIdentifier(peopleTop3_Vote[1].image, "drawable", packageName)!!
            //profile_rank_no2.img_profile.setImageResource(imageID)
            Glide.with(profile_rank_no2.img_profile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .fitCenter()
                .into(profile_rank_no2.img_profile)
            profile_rank_no2.text_name.text = peopleTop3_Vote[1].name
            profile_rank_no2.text_count.text = "${decimalFormat.format(peopleTop3_Vote[1].count)}???"
        }
        if (peopleTop3_Vote.size > 2 && profile_rank_no3 != null) {
            profile_rank_no3.visibility = View.VISIBLE
            imageID = resources?.getIdentifier(peopleTop3_Vote[2].image, "drawable", packageName)!!
            //profile_rank_no3.img_profile.setImageResource(imageID)
            Glide.with(profile_rank_no3.img_profile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .fitCenter()
                .into(profile_rank_no3.img_profile)
            profile_rank_no3.text_name.text = peopleTop3_Vote[2].name
            profile_rank_no3.text_count.text = "${decimalFormat.format(peopleTop3_Vote[2].count)}???"
        }
    }

    fun ShowCheeringTop3() {
        var imageID : Int = 0

        if (peopleTop3_Cheering.size > 0 && profile_rank_no1 != null) {
            profile_rank_no1.visibility = View.VISIBLE
            imageID = resources?.getIdentifier(peopleTop3_Cheering[0].image, "drawable", packageName)!!
            //profile_rank_no1.img_profile.setImageResource(imageID)
            Glide.with(profile_rank_no1.img_profile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .fitCenter()
                .into(profile_rank_no1.img_profile)
            profile_rank_no1.text_name.text = peopleTop3_Cheering[0].name
            profile_rank_no1.text_count.text = "${decimalFormat.format(peopleTop3_Cheering[0].cheeringCountTotal)}???"
        }
        if (peopleTop3_Cheering.size > 1 && profile_rank_no2 != null) {
            profile_rank_no2.visibility = View.VISIBLE
            imageID = resources?.getIdentifier(peopleTop3_Cheering[1].image, "drawable", packageName)!!
            //profile_rank_no2.img_profile.setImageResource(imageID)
            Glide.with(profile_rank_no2.img_profile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .fitCenter()
                .into(profile_rank_no2.img_profile)
            profile_rank_no2.text_name.text = peopleTop3_Cheering[1].name
            profile_rank_no2.text_count.text = "${decimalFormat.format(peopleTop3_Cheering[1].cheeringCountTotal)}???"
        }
        if (peopleTop3_Cheering.size > 2 && profile_rank_no3 != null) {
            profile_rank_no3.visibility = View.VISIBLE
            imageID = resources?.getIdentifier(peopleTop3_Cheering[2].image, "drawable", packageName)!!
            //profile_rank_no3.img_profile.setImageResource(imageID)
            Glide.with(profile_rank_no3.img_profile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .fitCenter()
                .into(profile_rank_no3.img_profile)
            profile_rank_no3.text_name.text = peopleTop3_Cheering[2].name
            profile_rank_no3.text_count.text = "${decimalFormat.format(peopleTop3_Cheering[2].cheeringCountTotal)}???"
        }
    }

    fun ShowMillionTop3(peopleTop3 : ArrayList<RankDTO>) {
        var imageID : Int = 0

        if (peopleTop3.size > 0 && profile_rank_no1 != null) {
            profile_rank_no1.visibility = View.VISIBLE
            imageID = resources?.getIdentifier(peopleTop3[0].image, "drawable", packageName)!!
            //profile_rank_no1.img_profile.setImageResource(imageID)
            Glide.with(profile_rank_no1.img_profile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .fitCenter()
                .into(profile_rank_no1.img_profile)
            profile_rank_no1.text_name.text = peopleTop3[0].name
            if (peopleTop3[0].updateDate != null)
                profile_rank_no1.text_count.text = "${SimpleDateFormat("yyyy-MM-dd").format(peopleTop3[0].updateDate)} ??????"
        }
        if (peopleTop3.size > 1 && profile_rank_no2 != null) {
            profile_rank_no2.visibility = View.VISIBLE
            imageID = resources?.getIdentifier(peopleTop3[1].image, "drawable", packageName)!!
            //profile_rank_no2.img_profile.setImageResource(imageID)
            Glide.with(profile_rank_no2.img_profile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .fitCenter()
                .into(profile_rank_no2.img_profile)
            profile_rank_no2.text_name.text = peopleTop3[1].name
            if (peopleTop3[1].updateDate != null)
                profile_rank_no2.text_count.text = "${SimpleDateFormat("yyyy-MM-dd").format(peopleTop3[1].updateDate)} ??????"
        }
        if (peopleTop3.size > 2 && profile_rank_no3 != null) {
            profile_rank_no3.visibility = View.VISIBLE
            imageID = resources?.getIdentifier(peopleTop3[2].image, "drawable", packageName)!!
            //profile_rank_no3.img_profile.setImageResource(imageID)
            Glide.with(profile_rank_no3.img_profile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .fitCenter()
                .into(profile_rank_no3.img_profile)
            profile_rank_no3.text_name.text = peopleTop3[2].name
            if (peopleTop3[2].updateDate != null)
                profile_rank_no3.text_count.text = "${SimpleDateFormat("yyyy-MM-dd").format(peopleTop3[2].updateDate)} ??????"
        }
    }

    fun ShowDonation() {
        var imageID : Int = 0

        if (peopleTop3_Vote.size > 0 && profile_rank_no1 != null) {
            profile_rank_no1.visibility = View.VISIBLE
            imageID = resources?.getIdentifier(peopleTop3_Vote[0].image, "drawable", packageName)!!
            //profile_rank_no1.img_profile.setImageResource(imageID)
            Glide.with(profile_rank_no1.img_profile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .fitCenter()
                .into(profile_rank_no1.img_profile)
            profile_rank_no1.text_name.text = peopleTop3_Vote[0].name
            profile_rank_no1.text_count.text = "${decimalFormat.format(peopleTop3_Vote[0].count?.div(1000000)?.times(10)?.plus(30))}?????? ??????"
        }
        if (peopleTop3_Vote.size > 1 && profile_rank_no2 != null) {
            profile_rank_no2.visibility = View.VISIBLE
            imageID = resources?.getIdentifier(peopleTop3_Vote[1].image, "drawable", packageName)!!
            //profile_rank_no2.img_profile.setImageResource(imageID)
            Glide.with(profile_rank_no2.img_profile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .fitCenter()
                .into(profile_rank_no2.img_profile)
            profile_rank_no2.text_name.text = peopleTop3_Vote[1].name
            profile_rank_no2.text_count.text = "${decimalFormat.format(peopleTop3_Vote[1].count?.div(1000000)?.times(10)?.plus(20))}?????? ??????"
        }
        if (peopleTop3_Vote.size > 2 && profile_rank_no3 != null) {
            profile_rank_no3.visibility = View.VISIBLE
            imageID = resources?.getIdentifier(peopleTop3_Vote[2].image, "drawable", packageName)!!
            //profile_rank_no3.img_profile.setImageResource(imageID)
            Glide.with(profile_rank_no3.img_profile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .fitCenter()
                .into(profile_rank_no3.img_profile)
            profile_rank_no3.text_name.text = peopleTop3_Vote[2].name
            profile_rank_no3.text_count.text = "${decimalFormat.format(peopleTop3_Vote[2].count?.div(1000000)?.times(10)?.plus(10))}?????? ??????"
        }
    }

    fun display() {
        profile_rank_no1.visibility = View.GONE
        profile_rank_no2.visibility = View.GONE
        profile_rank_no3.visibility = View.GONE

        when(displayTypes[displayTypeIndex]) {
            DISPLAY_TYPE.VOTE -> {
                button_title.text = "- ?????? ?????? Top 7 -"
                ShowVoteTop3()
                recyclerView.adapter = RecyclerViewAdapterRank(peopleOther_Vote, this)
            }
            DISPLAY_TYPE.CHEERING -> {
                button_title.text = "- ?????? ?????? Top 7 -"
                ShowCheeringTop3()
                recyclerView.adapter = RecyclerViewAdapterCheering(peopleOther_Cheering, this)
            }
            /*DISPLAY_TYPE.ONE_MILLION -> {
                button_title.text = "- 100?????? ?????? ?????? -"
                ShowMillionTop3(peopleTop3_OneMillon)
                recyclerView.adapter = RecyclerViewAdapterRank(peopleOther_OneMillon, this)
            }
            DISPLAY_TYPE.TWO_MILLION -> {
                button_title.text = "- 200?????? ?????? ?????? -"
                ShowMillionTop3(peopleTop3_TwoMillon)
                recyclerView.adapter = RecyclerViewAdapterRank(peopleOther_TwoMillon, this)
            }
            DISPLAY_TYPE.THREE_MILLION -> {
                button_title.text = "- 300?????? ?????? ?????? -"
                ShowMillionTop3(peopleTop3_ThreeMillon)
                recyclerView.adapter = RecyclerViewAdapterRank(peopleOther_ThreeMillon, this)
            }
            DISPLAY_TYPE.FOUR_MILLION -> {
                button_title.text = "- 400?????? ?????? ?????? -"
                ShowMillionTop3(peopleTop3_FourMillon)
                recyclerView.adapter = RecyclerViewAdapterRank(peopleOther_FourMillon, this)
            }
            DISPLAY_TYPE.FIVE_MILLION -> {
                button_title.text = "- 500?????? ?????? ?????? -"
                ShowMillionTop3(peopleTop3_FiveMillon)
                recyclerView.adapter = RecyclerViewAdapterRank(peopleOther_FiveMillon, this)
            }
            DISPLAY_TYPE.SIX_MILLION -> {
                button_title.text = "- 600?????? ?????? ?????? -"
                ShowMillionTop3(peopleTop3_SixMillon)
                recyclerView.adapter = RecyclerViewAdapterRank(peopleOther_SixMillon, this)
            }*/
            DISPLAY_TYPE.DONATION -> {
                button_title.text = "- ??? ?????? ?????? ??? -"
                ShowDonation()
                recyclerView.adapter = RecyclerViewAdapterRank(peopleOther_Donation, this)
            }
        }
    }

    fun loading() {
        android.os.Handler().postDelayed({
            if (loadingDialog == null) {
                loadingDialog = LoadingDialog(this)
                loadingDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                loadingDialog?.setCanceledOnTouchOutside(false)
            }
            loadingDialog?.show()
        }, 0)
    }

    fun loadingEnd() {
        android.os.Handler().postDelayed({
            loadingDialog?.dismiss()
        }, 400)
    }

    fun showAd() {
        // ?????? ?????? ??????
        var firestore = FirebaseFirestore.getInstance()
        firestore?.collection("preferences")?.document("ad_policy")?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var ad_interstitial = task.result!!["ad_interstitial"]
                callInterstitial(ad_interstitial as String)
            }
        }
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
        // ????????? - ??????
        var InterstitialAd = InterstitialAd(this)
        InterstitialAd.adUnitId = getString(R.string.admob_Interstitial_ad_unit_id) // ???????????????
        InterstitialAd.loadAd(AdRequest.Builder().build())

        InterstitialAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                if (InterstitialAd.isLoaded) {
                    InterstitialAd.show()
                } else {
                    // ?????? ?????? ??????
                    if (isFirst) {
                        interstitialCauly(false)
                    }
                }
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                // Code to be executed when an ad request fails.
                if (isFirst) {
                    interstitialCauly(false)
                } else {
                    if (isFinished)
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
                if (isFinished)
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
                    if (isFinished)
                        finish()
                }
            }

            override fun onClosedInterstitialAd(p0: CaulyInterstitialAd?) {
                if (isFinished)
                    finish()
            }

            override fun onLeaveInterstitialAd(p0: CaulyInterstitialAd?) {

            }

        }

        interstial.setInterstialAdListener(adCallback)
        interstial.requestInterstitialAd(this)
    }

    override fun onItemClick(item: BoardDTO, position: Int) {

    }

    override fun onItemClick_like(item: BoardDTO, like: TextView) {

    }

    override fun onItemClick_dislike(item: BoardDTO, dislike: TextView) {

    }

    fun InitAd() {

        // ????????? - ??????
        MobileAds.initialize(this, getString(R.string.admob_app_id))
        var adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        // ?????? ?????? ??????
        firestore?.collection("preferences")?.document("ad_policy")?.get()?.addOnCompleteListener { task ->
            if(task.isSuccessful){
                adType = task.result!!["ad_banner2"].toString() // ???????????????

                adviewVisible()
            }
        }
    }

    fun adviewVisible() {
        when(adType) {
            getString(R.string.adtype_admob) -> {
                // ????????? ??????
                mAdView.setVisible(true)

                // ????????? ?????????
                mAdViewCauly.setVisible(false)
                adView_kakao.setVisible(false)
            }
            getString(R.string.adtype_cauly) -> {
                // ????????? ??????
                mAdViewCauly.setVisible(true)

                // ????????? ?????????
                mAdView.setVisible(false)
                adView_kakao.setVisible(false)
            }
            getString(R.string.adtype_adfit) -> {
                // ????????? ??????
                adView_kakao.setVisible(true)

                val adView_kakao = adView_kakao!!  // ?????? ?????? ???
                adView_kakao.setClientId("DAN-U6IraHWngRlaqTr8")  // ?????? ?????? ?????? ??????(clientId) ??????
                adView_kakao.setAdListener(object : com.kakao.adfit.ads.AdListener {  // ?????? ?????? ????????? ??????

                    override fun onAdLoaded() {
                        //toast("Banner is loaded")
                    }

                    override fun onAdFailed(errorCode: Int) {
                        //toast("Failed to load banner :: errorCode = $errorCode")
                    }

                    override fun onAdClicked() {
                        //toast("Banner is clicked")
                    }

                })

                // lifecycle ?????? ????????? ??????
                // ?????? :: https://developer.android.com/topic/libraries/architecture/lifecycle
                // ?????? ???????????? ????????? BannerJava320x50Activity ??????
                lifecycle.addObserver(object : LifecycleObserver {

                    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
                    fun onResume() {
                        adView_kakao.resume()
                    }

                    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                    fun onPause() {
                        adView_kakao.pause()
                    }

                    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                    fun onDestroy() {
                        adView_kakao.destroy()
                    }

                })

                adView_kakao.loadAd()  // ?????? ??????

                // ????????? ?????????
                mAdView.setVisible(false)
                mAdViewCauly.setVisible(false)
            }
            else -> {
                // ?????? ?????????
                layout_adview.visibility  = View.GONE
            }
        }
    }

    fun View.setVisible(visible: Boolean) {
        visibility = if (visible) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun onItemClick(item: RankDTO, position: Int) {
        when(displayTypes[displayTypeIndex]) {
            DISPLAY_TYPE.DONATION, DISPLAY_TYPE.VOTE -> {
                callDonationCertificateDialog(item, position.plus(4))
            }
        }
    }

    fun callDonationCertificateDialog(item: RankDTO, rank: Int) {
        var donationNewsDTO : ArrayList<DonationNewsDTO> = arrayListOf()
        firestore?.collection("season_result")?.document(selectedSeason)?.collection("vote")?.document(item.docname.toString())?.collection("donationNews")?.orderBy("order", Query.Direction.ASCENDING)?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var news = document.toObject(DonationNewsDTO::class.java)!!
                donationNewsDTO.add(news)

                println("???????????? $news")
            }

            val dialog = DonationCertificateDialog(this, item, rank, donationNewsDTO)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            // Dialog ????????? ?????? ??????
            var params = dialog.window?.attributes!!
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.MATCH_PARENT
            dialog.window?.attributes = params
            dialog.button_cancel.setOnClickListener { // No
                dialog.dismiss()
            }
        }


    }
}