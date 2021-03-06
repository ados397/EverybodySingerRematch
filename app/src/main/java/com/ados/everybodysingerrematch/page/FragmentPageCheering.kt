package com.ados.everybodysingerrematch.page


import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AbsListView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.everybodysingerrematch.*
import com.ados.everybodysingerrematch.dialog.BoardDialog
import com.ados.everybodysingerrematch.dialog.BoardWriteDialog
import com.ados.everybodysingerrematch.dialog.LoadingDialog
import com.ados.everybodysingerrematch.dialog.QuestionDialog
import com.ados.everybodysingerrematch.model.*
import com.bumptech.glide.Glide
import com.fsn.cauly.CaulyAdInfo
import com.fsn.cauly.CaulyAdInfoBuilder
import com.fsn.cauly.CaulyInterstitialAd
import com.fsn.cauly.CaulyInterstitialAdListener
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.board_dialog.button_cancel
import kotlinx.android.synthetic.main.board_dialog.button_block
import kotlinx.android.synthetic.main.cheering_top_item.view.*
import kotlinx.android.synthetic.main.fragment_fragment_page_cheering.*
import kotlinx.android.synthetic.main.fragment_fragment_page_cheering.button_refresh
import kotlinx.android.synthetic.main.fragment_fragment_page_cheering.img_rank_background
import kotlinx.android.synthetic.main.fragment_fragment_page_cheering.img_season_logo
import kotlinx.android.synthetic.main.fragment_fragment_page_cheering.swipe_refresh_layout
import kotlinx.android.synthetic.main.fragment_fragment_page_rank.*
import kotlinx.android.synthetic.main.question_dialog.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timer

class FragmentPageCheering : Fragment(), OnCheeringItemClickListener {
    enum class ViewType {
        POPULAR, NEW, STATISTICS
    }

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")
    var dbHandler : DatabaseHelper? = null
    var firestore : FirebaseFirestore? = null
    lateinit var recyclerView : RecyclerView
    private var posts_popular : ArrayList<BoardDTO> = arrayListOf()
    private var posts_new : ArrayList<BoardDTO> = arrayListOf()
    private var people : ArrayList<RankDTO> = arrayListOf()
    private var statistics : ArrayList<BoardDTO> = arrayListOf()
    var pageIndex : Int? = 0
    var lastVisible : DocumentSnapshot? = null
    var isScrolling = false
    var isViewType = ViewType.POPULAR
    private var isrefresh = true
    var cheeringboardCollectionName = ""
    var peopleCollectionName = ""
    var preferencesDTO : PreferencesDTO? = null

    var loadingDialog : LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_fragment_page_cheering, container, false)

        var rootView = inflater.inflate(R.layout.fragment_fragment_page_cheering, container, false)
        recyclerView = rootView.findViewById(R.id.recyclerview_cheering!!)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        dbHandler = DatabaseHelper(getActivity()!!)
        firestore = FirebaseFirestore.getInstance()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore?.collection("preferences")?.document("season")?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            var seasonDTO = documentSnapshot?.toObject(SeasonDTO::class.java)
            peopleCollectionName = "people_cheering"

            // ?????? ?????? ??????
            //seasonDTO?.seasonNum = 2
            var rank_background_img = R.drawable.spotlight_s1_cheering
            var season_logo_img = R.drawable.season2_logo
            if (seasonDTO?.seasonNum == 1) {
                cheeringboardCollectionName = "cheeringboard_s1"
                rank_background_img = R.drawable.spotlight_s1_cheering
                season_logo_img = R.drawable.season1_logo
                img_season_logo.visibility = View.GONE
            } else {
                cheeringboardCollectionName = "cheeringboard_s2"
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
        }

        firestore?.collection("preferences")?.document("preferences")?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            preferencesDTO = documentSnapshot?.toObject(PreferencesDTO::class.java)
        }

        layout_top1.visibility = View.GONE
        layout_top2.visibility = View.GONE
        layout_top3.visibility = View.GONE
        //layout_top1.img_crown.setImageResource(R.drawable.crown_gold)
        //layout_top2.img_crown.setImageResource(R.drawable.crown_silver)
        //layout_top3.img_crown.setImageResource(R.drawable.crown_bronze)
        Glide.with(layout_top1.img_crown.context)
            .asBitmap()
            .load(R.drawable.crown_gold) ///feed in path of the image
            .fitCenter()
            .into(layout_top1.img_crown)
        Glide.with(layout_top2.img_crown.context)
            .asBitmap()
            .load(R.drawable.crown_silver) ///feed in path of the image
            .fitCenter()
            .into(layout_top2.img_crown)
        Glide.with(layout_top3.img_crown.context)
            .asBitmap()
            .load(R.drawable.crown_bronze) ///feed in path of the image
            .fitCenter()
            .into(layout_top3.img_crown)

        Glide.with(img_question.context)
            .asBitmap()
            .load(R.drawable.question) ///feed in path of the image
            .fitCenter()
            .into(img_question)

        Glide.with(layout_top3.img_crown.context)
            .asBitmap()
            .load(R.drawable.crown_bronze) ///feed in path of the image
            .fitCenter()
            .into(layout_top3.img_crown)

        number_picker.minValue = 0
        number_picker.maxValue = 47
        number_picker.wrapSelectorWheel = false

        loading()
        timer(period = 100)
        {
            if (cheeringboardCollectionName.isNotEmpty() && peopleCollectionName.isNotEmpty()) {
                cancel()
                getActivity()!!.runOnUiThread {
                    isViewType = ViewType.NEW
                    refreshData(posts_new)

                    isViewType = ViewType.POPULAR
                    refreshData(posts_popular)

                    refreshPeople()
                }
            }
        }
        timer(period = 100)
        {
            if (posts_popular.size > 0) {
                cancel()
                getActivity()!!.runOnUiThread {
                    showPopular()
                    loadingEnd()
                }
            }
        }
        //refreshStatistics()

        button_write.setOnClickListener {
            var nowDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
            var pref = PreferenceManager.getDefaultSharedPreferences(getActivity())
            var writeDate = pref.getString("WriteCheering", "")

            if (nowDate == writeDate) {
                Toast.makeText(getActivity(),"???????????? ????????? ????????? ??? ??? ????????????.", Toast.LENGTH_SHORT).show()
            } else {
                var cheerBoard = pref.getString("CheerBoard", "")
                if (cheerBoard.isNullOrEmpty()) {
                    var question = QuestionDTO(QuestionDTO.STAT.INFO, "????????? ????????????", "")
                    question.content = """
????????? ??? ????????? ???????????? ?????? ???????????? ???????????? ??????????????? ???????????? ????????? ?????? ??? ????????????.

??? ?????????????????? ???????????? ?????? ?????? ?????? ??????????????? ???????????? ???????????? ????????? ???
??? ???????????? ??????????????? ??????????????? ????????? ?????? ??????
??? ?????? ???????????? ????????? ???????????? ?????? ??????
                """
                    val questionDialog = QuestionDialog(requireContext(), question)
                    questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    questionDialog.setCanceledOnTouchOutside(false)
                    questionDialog.show()
                    questionDialog.setButtonOk("??????")
                    questionDialog.setButtonCancel("??????")

                    questionDialog.button_question_cancel.setOnClickListener { // No
                        questionDialog.dismiss()
                        Toast.makeText(getActivity(),"????????? ??????????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show()
                    }

                    questionDialog.button_question_ok.setOnClickListener { // Ok
                        questionDialog.dismiss()

                        // ?????? ?????? ??????
                        var editor = pref.edit()
                        editor.putString("CheerBoard", nowDate).apply()

                        Toast.makeText(getActivity(),"$nowDate ?????? ?????? ???????????????.", Toast.LENGTH_SHORT).show()

                        showBoardWriteDialog()
                    }
                } else {
                    showBoardWriteDialog()
                }
            }
        }

        button_refresh.setOnClickListener {
            if (isrefresh == false) {
                Toast.makeText(getActivity(),"??????????????? 5?????? ??? ??? ???????????????.", Toast.LENGTH_SHORT).show()
            } else {
                isrefresh = false

                var second = 1;
                timer(period = 1000)
                {
                    if (second > 5) {
                        cancel()
                        isrefresh = true
                    }
                    second++
                }

                lastVisible = null
                if (isViewType == ViewType.STATISTICS) {
                    refreshPeople()
                } else {
                    if (isViewType == ViewType.POPULAR) {
                        refreshData(posts_popular)
                    } else if (isViewType == ViewType.NEW) {
                        refreshData(posts_new)
                    }
                }
            }
        }

        swipe_refresh_layout.setOnRefreshListener {
            if (isrefresh == false) {
                Toast.makeText(getActivity(),"??????????????? 5?????? ??? ??? ???????????????.", Toast.LENGTH_SHORT).show()
                swipe_refresh_layout.setRefreshing(false)
            } else {
                isrefresh = false

                var second = 1;
                timer(period = 1000)
                {
                    if (second > 5) {
                        cancel()
                        isrefresh = true
                    }
                    second++
                }

                if (isViewType != ViewType.STATISTICS) {
                    lastVisible = null
                    if (isViewType == ViewType.POPULAR) {
                        refreshData(posts_popular)
                    } else if (isViewType == ViewType.NEW) {
                        refreshData(posts_new)
                    }
                }
                swipe_refresh_layout.setRefreshing(false)
            }
        }

        text_popular.setOnClickListener {
            if (isViewType != ViewType.POPULAR) {
                showPopular()
            }
        }

        text_new.setOnClickListener {
            if (isViewType != ViewType.NEW) {
                showNew()
            }
        }

        text_statistics.setOnClickListener {
            if (isViewType != ViewType.STATISTICS) {
                showStatistics()
            }
        }

        img_question.setOnClickListener {
            var question = QuestionDTO(QuestionDTO.STAT.INFO, "???????????? ?????? ??? ??????", "")
            question.content = """
??? ???????????? ????????? ?????? ??? ???
    ?????? 0???~2??? ????????? ?????????.
??? ?????? ??????
    - ????????? ?????? : 50???, ????????? : 1???
    - ??? ????????? ?????? ????????? ?????? ??????
                """
            val dialog = QuestionDialog(requireContext(), question)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.setButtonCancel("??????")
            dialog.showButtonOk(false)

            dialog.button_question_cancel.setOnClickListener { // No
                dialog.dismiss()
            }
        }

        rayout_number_picker.visibility = View.GONE
        //button_data.visibility = View.GONE // ???????????????
        button_data.setOnClickListener {
            refreshStatistics()
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                //val totalItemCount = recyclerView.layoutManager!!.itemCount
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true
                }
            }
            override fun onScrolled(recyclerView1: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView1, dx, dy)

                if (isViewType == ViewType.POPULAR) {
                    if (!recyclerView.canScrollVertically(1)) {
                        if (posts_popular.size >= 30) {
                            isScrolling = false

                            refreshData(posts_popular)
                        }
                    }
                } else if (isViewType == ViewType.NEW) {
                    if (!recyclerView.canScrollVertically(1)) {
                        if (posts_new.size >= 30) {
                            isScrolling = false

                            refreshData(posts_new)
                        }
                    }
                }
            }
        })
    }

    fun showBoardWriteDialog() {
        val dialog = BoardWriteDialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        dialog.button_cancel.setOnClickListener { // No
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            if (dialog.isWrite == true) {
                // ????????? 1??? ??????
                var pref = PreferenceManager.getDefaultSharedPreferences(getActivity())
                var ticketcount = pref.getInt("TicketCount", preferencesDTO?.ticketChargeCount!!)

                var editor = pref.edit()
                editor.putInt("TicketCount", ticketcount + 1).apply()

                Toast.makeText(getActivity(),"??????????????? ????????? 1??? ?????????????????????.", Toast.LENGTH_SHORT).show()

                showAd()
            }
        }
    }

    fun refreshData(posts : ArrayList<BoardDTO>) {
        loading()
        var field = ""
        if (isViewType == ViewType.POPULAR) {
            field = "likeCount"
        } else {
            field = "time"
        }

        /* ?????? ?????? ??????????????? ????????? ?????? ????????? ?????????
        ?.whereGreaterThanOrEqualTo("time", cal.time)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val limit = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        val cal = Calendar.getInstance()
        cal.time = dateFormat.parse(limit)
        cal.add (Calendar.DATE, -90)*/

        if (lastVisible == null) {
            firestore?.collection(cheeringboardCollectionName)?.orderBy(field, Query.Direction.DESCENDING)
                ?.limit(30)?.get()?.addOnSuccessListener { result ->
                posts.clear()
                for (document in result) {
                    var person = document.toObject(BoardDTO::class.java)!!
                    if (dbHandler?.getblock(person.docname.toString()) == true) {
                        person.isBlock = true
                    }
                    posts.add(person)
                    lastVisible = result.documents.get(result.size() - 1)
                }
                if (result.size() > 0) {
                    recyclerView.adapter = RecyclerViewAdapterCheering(posts, this)
                }
                loadingEnd()
            }?.addOnFailureListener { exception ->

            }
        } else {
            firestore?.collection(cheeringboardCollectionName)?.orderBy(field, Query.Direction.DESCENDING)?.startAfter(lastVisible!!)?.limit(30)?.get()?.addOnSuccessListener { result ->
                //posts.clear()
                for (document in result) {
                    var board = document.toObject(BoardDTO::class.java)!!
                    if (dbHandler?.getblock(board.docname.toString()) == true) {
                        board.isBlock = true
                    }
                    posts.add(board)
                    lastVisible = result.documents.get(result.size() - 1)
                }
                //recyclerView.adapter = RecyclerViewAdapterCheering(posts, this)
                if (result.size() > 0) {
                    recyclerView.adapter?.notifyItemInserted(posts.size)
                }
                loadingEnd()
            }?.addOnFailureListener { exception ->

            }
        }
    }

    fun showPopular() {
        isViewType = ViewType.POPULAR
        lastVisible = null

        text_popular.setTextColor(Color.parseColor("#DDFFF319"))
        text_popular.paintFlags = text_popular.paintFlags or Paint.UNDERLINE_TEXT_FLAG or Paint.FAKE_BOLD_TEXT_FLAG
        text_new.setTextColor(Color.parseColor("#CCCCCC"))
        text_new.paintFlags = Paint.ANTI_ALIAS_FLAG
        text_statistics.setTextColor(Color.parseColor("#CCCCCC"))
        text_statistics.paintFlags = Paint.ANTI_ALIAS_FLAG

        recyclerView.adapter = RecyclerViewAdapterCheering(posts_popular, this)
        /*if (isViewType == ViewType.POPULAR) {
            refreshData(posts_popular)
        } else if (isViewType == ViewType.NEW) {
            refreshData(posts_new)
        }*/
    }

    fun showNew() {
        isViewType = ViewType.NEW
        lastVisible = null

        text_popular.setTextColor(Color.parseColor("#CCCCCC"))
        text_popular.paintFlags = Paint.ANTI_ALIAS_FLAG
        text_new.setTextColor(Color.parseColor("#DDFFF319"))
        text_new.paintFlags = text_popular.paintFlags or Paint.UNDERLINE_TEXT_FLAG or Paint.FAKE_BOLD_TEXT_FLAG
        text_statistics.setTextColor(Color.parseColor("#CCCCCC"))
        text_statistics.paintFlags = Paint.ANTI_ALIAS_FLAG

        recyclerView.adapter = RecyclerViewAdapterCheering(posts_new, this)
        /*if (isViewType == ViewType.POPULAR) {
            refreshData(posts_popular)
        } else if (isViewType == ViewType.NEW) {
            refreshData(posts_new)
        }*/
    }

    fun showStatistics() {
        isViewType = ViewType.STATISTICS
        lastVisible = null

        text_popular.setTextColor(Color.parseColor("#CCCCCC"))
        text_popular.paintFlags = Paint.ANTI_ALIAS_FLAG
        text_new.setTextColor(Color.parseColor("#CCCCCC"))
        text_new.paintFlags = Paint.ANTI_ALIAS_FLAG
        text_statistics.setTextColor(Color.parseColor("#DDFFF319"))
        text_statistics.paintFlags = text_popular.paintFlags or Paint.UNDERLINE_TEXT_FLAG or Paint.FAKE_BOLD_TEXT_FLAG

        recyclerView.adapter = RecyclerViewAdapterCheering(statistics, this)
    }

    override fun onItemClick(item: BoardDTO, position: Int) {
        if (isViewType != ViewType.STATISTICS) {
            val dialog = BoardDialog(requireContext(), item, getActivity()!!)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.button_cancel.setOnClickListener {
                // No
                dialog.dismiss()
            }
            dialog.button_block.setOnClickListener {
                var question = QuestionDTO(
                    QuestionDTO.STAT.ERROR,
                    "????????? ??????",
                    "?????? ???????????? ?????? ???????????????????"
                )
                if (item.isBlock) {
                    question.title = "????????? ?????? ??????"
                    question.content = "?????? ???????????? ?????? ?????? ???????????????????"
                }

                val questionDialog = QuestionDialog(requireContext(), question)
                questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                questionDialog.setCanceledOnTouchOutside(false)
                questionDialog.show()
                questionDialog.button_question_cancel.setOnClickListener { // No
                    questionDialog.dismiss()
                }
                questionDialog.button_question_ok.setOnClickListener { // Ok
                    questionDialog.dismiss()
                    dialog.dismiss()
                    if (dbHandler?.getblock(item.docname.toString()) == false) {
                        dbHandler?.updateBlock(item.docname.toString(), 1)
                        Toast.makeText(context,"????????? ??????", Toast.LENGTH_SHORT).show()
                    } else {
                        dbHandler?.updateBlock(item.docname.toString(), 0)
                        Toast.makeText(context,"????????? ?????? ??????", Toast.LENGTH_SHORT).show()
                    }

                    lastVisible = null
                    if (isViewType == ViewType.STATISTICS) {
                        refreshPeople()
                    } else {
                        if (isViewType == ViewType.POPULAR) {
                            refreshData(posts_popular)
                        } else if (isViewType == ViewType.NEW) {
                            refreshData(posts_new)
                        }
                    }
                }
            }
        }
    }

    override fun onItemClick_like(item: BoardDTO, like: TextView) {
        if (dbHandler?.getlike(item.docname.toString()) == false) {
            dbHandler?.updateLike(item.docname.toString(), 1)

            item.likeCount = item.likeCount!! + 1
            like.text = "${item.likeCount}"
            like.paintFlags = like.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            Toast.makeText(getActivity(),"?????????", Toast.LENGTH_SHORT).show()
        } else {
            dbHandler?.updateLike(item.docname.toString(), 0)

            item.likeCount = item.likeCount!! - 1
            like.text = "${item.likeCount}"
            like.paintFlags = Paint.ANTI_ALIAS_FLAG

            Toast.makeText(getActivity(),"????????? ??????", Toast.LENGTH_SHORT).show()
        }


    }

    override fun onItemClick_dislike(item: BoardDTO, dislike: TextView) {
        if (dbHandler?.getdislike(item.docname.toString()) == false) {
            dbHandler?.updateDislike(item.docname.toString(), 1)

            item.dislikeCount = item.dislikeCount!! + 1
            dislike.text = "${item.dislikeCount}"
            dislike.paintFlags = dislike.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            Toast.makeText(getActivity(),"?????????", Toast.LENGTH_SHORT).show()
        } else {
            dbHandler?.updateDislike(item.docname.toString(), 0)

            item.dislikeCount = item.dislikeCount!! - 1
            dislike.text = "${item.dislikeCount}"
            dislike.paintFlags = Paint.ANTI_ALIAS_FLAG

            Toast.makeText(getActivity(),"????????? ??????", Toast.LENGTH_SHORT).show()
        }
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
        var InterstitialAd = InterstitialAd(context)
        InterstitialAd.adUnitId = getString(R.string.admob_Interstitial_ad_unit_id)
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
                }
            }

            override fun onClosedInterstitialAd(p0: CaulyInterstitialAd?) {

            }

            override fun onLeaveInterstitialAd(p0: CaulyInterstitialAd?) {

            }

        }

        interstial.setInterstialAdListener(adCallback)
        interstial.requestInterstitialAd(getActivity())
    }

    fun loading() {
        android.os.Handler().postDelayed({
            if (loadingDialog == null) {
                loadingDialog = LoadingDialog(requireContext())
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

    fun refreshPeople() {
        firestore?.collection(peopleCollectionName)?.orderBy("cheeringCountTotal", Query.Direction.DESCENDING)
            ?.get()?.addOnSuccessListener { result ->
                people.clear()
                statistics.clear()
                var index = 0
                for (document in result) {
                    var person = document.toObject(RankDTO::class.java)!!
                    people.add(person)
                    if (index == 0 && layout_top1 != null) {
                        layout_top1.visibility = View.VISIBLE
                        layout_top1.text_name.text = person.name
                        layout_top1.text_count.text = "?????????:${decimalFormat.format(person.cheeringCount)}"
                        layout_top1.text_count2.text = "?????????:${decimalFormat.format(person.likeCount)}"
                    } else if (index == 1 && layout_top2 != null) {
                        layout_top2.visibility = View.VISIBLE
                        layout_top2.text_name.text = person.name
                        layout_top2.text_count.text = "?????????:${decimalFormat.format(person.cheeringCount)}"
                        layout_top2.text_count2.text = "?????????:${decimalFormat.format(person.likeCount)}"
                    } else if (index == 2 && layout_top3 != null) {
                        layout_top3.visibility = View.VISIBLE
                        layout_top3.text_name.text = person.name
                        layout_top3.text_count.text = "?????????:${decimalFormat.format(person.cheeringCount)}"
                        layout_top3.text_count2.text = "?????????:${decimalFormat.format(person.likeCount)}"
                    }

                    var board = BoardDTO()
                    board.image = person.image
                    board.title = "${index+1}??? ${person.name}"
                    board.content = "?????? ?????? : ${decimalFormat.format(person.cheeringCountTotal)}"
                    board.name = "????????? : ${decimalFormat.format(person.cheeringCount)}"
                    board.likeCount = person.likeCount
                    board.dislikeCount = person.dislikeCount
                    statistics.add(board)
                    index++
                }
            }?.addOnFailureListener { exception ->

            }
    }

    fun refreshStatistics() {
        Toast.makeText(getActivity(),"????????? ?????? ??????", Toast.LENGTH_SHORT).show()
        // 01????????? 03??? ????????? ??????????????? ???????????? ????????? ??????
        var nowTime = SimpleDateFormat("HHmm").format(Date()).toInt()
        //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P && (nowTime >= 0 && nowTime <= 2400)) {
            println("????????? ?????? ??????")
            var nowDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
            firestore?.collection(peopleCollectionName)?.get()?.addOnSuccessListener { result ->
                for (document in result) {
                    var person = document.toObject(RankDTO::class.java)!!
                    println("$person ?????? ??????")
                    // ??????????????? ?????? ????????? ??????
                    var updateDate = ""
                    if (person.updateDate != null) {
                        updateDate = SimpleDateFormat("yyyy-MM-dd").format(person.updateDate)
                    }
                    println("???????????? : $nowDate, ?????????????????? : $updateDate")

                    SystemClock.sleep(300)
                    if (number_picker.value == 0 || person.image.equals("profile${String.format("%03d",number_picker.value)}")) {
                    //if (nowDate != updateDate) {
                        loading()
                        println("???????????? ??????")
                        firestore?.collection(cheeringboardCollectionName)?.whereEqualTo("image", person.image)
                            ?.get()?.addOnSuccessListener { result ->
                                person.cheeringCount = result.size()
                                person.likeCount = 0
                                person.dislikeCount = 0
                                person.updateDate = Date()
                                for (document in result) {
                                    var post = document.toObject(BoardDTO::class.java)!!
                                    if (post.likeCount!! < 55555) {
                                        person.likeCount = person.likeCount!! + post.likeCount!!
                                        person.dislikeCount =
                                            person.dislikeCount!! + post.dislikeCount!!
                                    }
                                }

                                // ?????? ????????? ????????? 50???, ????????? 1???
                                person.cheeringCountTotal = (person.cheeringCount!! * 50) + person.likeCount!!

                                firestore?.collection(peopleCollectionName)
                                    ?.document(person.docname.toString())
                                    ?.set(person)

                                loadingEnd()
                                println("$person ????????? ??????")
                                Toast.makeText(getActivity(),"[${person.name} ??????.]", Toast.LENGTH_SHORT).show()
                            }
                            ?.addOnFailureListener { exception ->

                            }
                    }
                }
            }
                ?.addOnFailureListener { exception ->

                }

            println("????????? ?????? ???")
        //}
    }
}
