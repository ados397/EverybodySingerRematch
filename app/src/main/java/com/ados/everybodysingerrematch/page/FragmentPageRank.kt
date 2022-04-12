package com.ados.everybodysingerrematch.page


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.annotation.Dimension
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.everybodysingerrematch.dialog.LoadingDialog
import com.ados.everybodysingerrematch.MainActivity
import com.ados.everybodysingerrematch.R
import com.ados.everybodysingerrematch.dialog.AdminDialog
import com.ados.everybodysingerrematch.dialog.DonationStatusDialog
import com.ados.everybodysingerrematch.model.RankDTO
import com.ados.everybodysingerrematch.model.SeasonDTO
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.donation_status_dialog.*
import kotlinx.android.synthetic.main.fragment_fragment_page_rank.*
import kotlinx.android.synthetic.main.fragment_fragment_page_rank.button_refresh
import kotlinx.android.synthetic.main.fragment_fragment_page_rank.profile_rank_no1
import kotlinx.android.synthetic.main.fragment_fragment_page_rank.profile_rank_no2
import kotlinx.android.synthetic.main.fragment_fragment_page_rank.profile_rank_no3
import kotlinx.android.synthetic.main.profile_item.view.*
import java.text.DecimalFormat
import kotlin.concurrent.timer

class FragmentPageRank : Fragment(), OnRankItemClickListener {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    var firestore : FirebaseFirestore? = null
    private var peopleTop3 : ArrayList<RankDTO> = arrayListOf()
    private var peopleOther : ArrayList<RankDTO> = arrayListOf()
    lateinit var recyclerView : RecyclerView

    private var isrefresh = true

    var loadingDialog : LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_fragment_page_rank, container, false)

        var rootView = inflater.inflate(R.layout.fragment_fragment_page_rank, container, false)
        recyclerView = rootView.findViewById(R.id.recyclerview_rank!!)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        var index : Int = 1
        firestore = FirebaseFirestore.getInstance()

        /*firestore?.collection("people")?.orderBy("count", Query.Direction.DESCENDING)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            peopleTop3.clear()
            peopleOther.clear()
            if(querySnapshot == null)return@addSnapshotListener

            index = 1
            // document 수만큼 획득
            for(snapshot in querySnapshot){
                var person = snapshot.toObject(RankDTO::class.java)!!
                if (index < 4) { // Top 3 저장
                    peopleTop3.add(person)
                    ShowTop3()
                }
                else { // 4위 부터 저장
                    peopleOther.add(person)
                }
                index++
            }
            recyclerView.adapter =
                RecyclerViewAdapterRank(
                    peopleOther
                )
        }*/
        refreshPeople()

        // 시즌 변경 작업
        /*var names : ArrayList<String> = arrayListOf(
            "강하윤"
            , "건지"
            , "고승형"
            , "고은성"
            , "권민제"
            , "권아린"
            , "권의빈"
            , "권지영"
            , "김국헌"
            , "김다윤"
            , "김대완"
            , "김대훈"
            , "김도하"
            , "김동하"
            , "김동현"
            , "김민수"
            , "김민욱"
            , "김성준"
            , "김영근"
            , "김영흠"
            , "김예현"
            , "김웅진"
            , "김유하"
            , "김윤희"
            , "김주왕"
            , "김태수"
            , "김현수"
            , "김현우"
            , "김형석"
            , "김희석"
            , "니다"
            , "데이비드 오"
            , "도유민"
            , "람람자매"
            , "류영채"
            , "마미썬"
            , "멋진녀석들"
            , "문건희"
            , "문세영"
            , "미나"
            , "박광선"
            , "박민호"
            , "박솔"
            , "박수호"
            , "박시환"
            , "박은영"
            , "박장현"
            , "박종민"
            , "박종원"
            , "박창근"
            , "방세진"
            , "범승혁"
            , "서라안"
            , "서지유"
            , "손보승"
            , "손진욱"
            , "송용식"
            , "스팍"
            , "시즈닝"
            , "신민경"
            , "안율"
            , "양기웅"
            , "염수민"
            , "유민"
            , "유슬기"
            , "유용민"
            , "유지니"
            , "육은비"
            , "윤정훈"
            , "이나빈"
            , "이병찬"
            , "이소원"
            , "이솔로몬"
            , "이송현"
            , "이승철"
            , "이시온"
            , "이안"
            , "이의진"
            , "이재은"
            , "이주천"
            , "이준안"
            , "이혁"
            , "임소리"
            , "임지민"
            , "임지수"
            , "임한별"
            , "장구남매"
            , "정모"
            , "정병희"
            , "정선국"
            , "정성"
            , "정은지"
            , "정한빈"
            , "조남준"
            , "조동건"
            , "조연호"
            , "조우준"
            , "지상"
            , "지세희"
            , "진웅"
            , "최상"
            , "최선우"
            , "최여원"
            , "최진솔"
            , "클래스메이트"
            , "표바하"
            , "하동연"
            , "하진우"
            , "한선천"
            , "한아름"
            , "3XY"
        )
        var index2 : Int = 1
        for (name in names){
            var docname = String.format("no%05d",index2)
            var person = RankDTO(String.format("profile%03d",index2), name, 0, docname)
            firestore?.collection("people")?.document(docname)?.set(person)
            firestore?.collection("people_cheering")?.document(docname)?.set(person)
            index2++
        }*/


        /*firestore?.collection("people")?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                firestore?.collection("people2")?.document(person.docname!!)?.set(person)
            }
        }
            ?.addOnFailureListener { exception ->

            }*/

        /*firestore?.collection("people")?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                person.count = 0
                firestore?.collection("people_back")?.document(person.docname.toString())?.set(person)
            }
        }
                ?.addOnFailureListener { exception ->

                }*/
        // 시즌 변경 작업
        /*firestore?.collection("people_back")?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                person.count = 0
                firestore?.collection("people")?.document(person.docname.toString())?.set(person)
            }
        }
                ?.addOnFailureListener { exception ->

                }*/


        //recyclerView.adapter = MyRankRecyclerViewAdapter(peopleOther)


        return rootView
    }

    fun refreshPeople() {
        loading()
        var index : Int = 1
        firestore?.collection("people")?.orderBy("count", Query.Direction.DESCENDING)?.get()?.addOnSuccessListener { result ->
            peopleTop3.clear()
            peopleOther.clear()

            index = 1
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                if (index < 4) { // Top 3 저장
                    peopleTop3.add(person)
                    ShowTop3()
                }
                else { // 4위 부터 저장
                    peopleOther.add(person)
                }
                index++
            }
            recyclerView.adapter = RecyclerViewAdapterRank(peopleOther, this)
        }
            ?.addOnFailureListener { exception ->

            }
        loadingEnd()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        firestore?.collection("preferences")?.document("season")?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            var seasonDTO = documentSnapshot?.toObject(SeasonDTO::class.java)

            // 시즌 변경 작업
            //seasonDTO?.seasonNum = 2
            //text_season_end_date.visibility = View.GONE
            //button_refresh.visibility = View.GONE
            var rank_background_img = R.drawable.spotlight_s2_main
            var season_logo_img = R.drawable.season2_logo
            if (seasonDTO?.seasonNum!! == 1) {
                rank_background_img = R.drawable.spotlight_s1_main
                season_logo_img = R.drawable.season1_logo
                img_hall_of_fame.visibility = View.GONE
                img_season_logo.visibility = View.GONE
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

            text_season_end_date.text = seasonDTO?.endDate
        }

        button_refresh.setOnClickListener {
            if (isrefresh == false) {
                Toast.makeText(getActivity(),"새로고침은 5초에 한 번 가능합니다.", Toast.LENGTH_SHORT).show()
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

                refreshPeople()
            }
        }

        img_hall_of_fame.setOnClickListener {
            (activity as MainActivity?)!!.callHallOfFameActivity()
        }

        //adapter 추가
        //recyclerview_rank.adapter = MyRankRecyclerViewAdapter()
        //레이아웃 매니저 추가
        //recyclerview_rank.layoutManager = LinearLayoutManager(view)
        //recyclerview_rank.layoutManager = RelativeLayoutManager(this)

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

        Glide.with(img_hall_of_fame.context)
            .asBitmap()
            .load(R.drawable.hall_of_fame_button) ///feed in path of the image
            .fitCenter()
            .into(img_hall_of_fame)

        /*var imageID = context?.resources?.getIdentifier(peopleTop3[0].image, "drawable", context?.packageName)
        if (imageID != null) {
            profile_rank_no1.img_profile.setImageResource(imageID)
        }
        imageID = context?.resources?.getIdentifier(peopleTop3[1].image, "drawable", context?.packageName)
        if (imageID != null) {
            profile_rank_no2.img_profile.setImageResource(imageID)
        }
        imageID = context?.resources?.getIdentifier(peopleTop3[2].image, "drawable", context?.packageName)
        if (imageID != null) {
            profile_rank_no3.img_profile.setImageResource(imageID)
        }*/

        profile_rank_no1.visibility = View.GONE
        profile_rank_no2.visibility = View.GONE
        profile_rank_no3.visibility = View.GONE

        profile_rank_no1.text_rank.text = "현재  1위"
        profile_rank_no1.text_rank.setTextSize(Dimension.SP, 10.toFloat())
        profile_rank_no1.text_name.setTextSize(Dimension.SP, 10.toFloat())
        profile_rank_no1.text_count.setTextSize(Dimension.SP, 10.toFloat())

        profile_rank_no2.text_rank.text = "현재  2위"
        profile_rank_no2.text_rank.setTextSize(Dimension.SP, 9.toFloat())
        profile_rank_no2.text_name.setTextSize(Dimension.SP, 9.toFloat())
        profile_rank_no2.text_count.setTextSize(Dimension.SP, 9.toFloat())

        profile_rank_no3.text_rank.text = "현재  3위"
        profile_rank_no3.text_rank.setTextSize(Dimension.SP, 8.toFloat())
        profile_rank_no3.text_name.setTextSize(Dimension.SP, 8.toFloat())
        profile_rank_no3.text_count.setTextSize(Dimension.SP, 8.toFloat())

        profile_rank_no1.setOnClickListener {
            callDonationStatusDialog(peopleTop3[0], 1)
        }
        profile_rank_no2.setOnClickListener {
            callDonationStatusDialog(peopleTop3[1], 2)
        }
        profile_rank_no3.setOnClickListener {
            callDonationStatusDialog(peopleTop3[2], 3)
        }

        //button_admin.visibility = View.GONE // 관리자모드
        button_admin.setOnClickListener {
            val dialog = AdminDialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.button_ok.setOnClickListener { // OK
                dialog.dismiss()
            }
        }
    }

    fun ShowTop3() {
        var imageID : Int = 0
        /*var imageID = context?.resources?.getIdentifier(peopleTop3[0].image, "drawable", context?.packageName)
        if (imageID != null) {
            profile_rank_no1.img_profile.setImageResource(imageID)
        }
        imageID = context?.resources?.getIdentifier(peopleTop3[1].image, "drawable", context?.packageName)
        if (imageID != null) {
            profile_rank_no2.img_profile.setImageResource(imageID)
        }
        imageID = context?.resources?.getIdentifier(peopleTop3[2].image, "drawable", context?.packageName)
        if (imageID != null) {
            profile_rank_no3.img_profile.setImageResource(imageID)
        }*/

        if (peopleTop3.size > 0 && profile_rank_no1 != null) {
            profile_rank_no1.visibility = View.VISIBLE
            imageID = context?.resources?.getIdentifier(peopleTop3[0].image, "drawable", context?.packageName)!!
            //profile_rank_no1.img_profile.setImageResource(imageID)
            Glide.with(profile_rank_no1.img_profile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .fitCenter()
                .into(profile_rank_no1.img_profile)
            profile_rank_no1.text_name.text = peopleTop3[0].name
            profile_rank_no1.text_count.text = "${decimalFormat.format(peopleTop3[0].count)}표"

            //profile_rank_no1.img_profile.visibility = View.GONE
            //profile_rank_no1.text_name.visibility = View.GONE
            //profile_rank_no1.text_count.visibility = View.GONE
        }
        if (peopleTop3.size > 1 && profile_rank_no2 != null) {
            profile_rank_no2.visibility = View.VISIBLE
            imageID = context?.resources?.getIdentifier(peopleTop3[1].image, "drawable", context?.packageName)!!
            //profile_rank_no2.img_profile.setImageResource(imageID)
            Glide.with(profile_rank_no2.img_profile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .fitCenter()
                .into(profile_rank_no2.img_profile)
            profile_rank_no2.text_name.text = peopleTop3[1].name
            profile_rank_no2.text_count.text = "${decimalFormat.format(peopleTop3[1].count)}표"

            //profile_rank_no2.img_profile.visibility = View.GONE
            //profile_rank_no2.text_name.visibility = View.GONE
            //profile_rank_no2.text_count.visibility = View.GONE
        }
        if (peopleTop3.size > 2 && profile_rank_no3 != null) {
            profile_rank_no3.visibility = View.VISIBLE
            imageID = context?.resources?.getIdentifier(peopleTop3[2].image, "drawable", context?.packageName)!!
            //profile_rank_no3.img_profile.setImageResource(imageID)
            Glide.with(profile_rank_no3.img_profile.context)
                .asBitmap()
                .load(imageID) ///feed in path of the image
                .fitCenter()
                .into(profile_rank_no3.img_profile)
            profile_rank_no3.text_name.text = peopleTop3[2].name
            profile_rank_no3.text_count.text = "${decimalFormat.format(peopleTop3[2].count)}표"

            //profile_rank_no3.img_profile.visibility = View.GONE
            //profile_rank_no3.text_name.visibility = View.GONE
            //profile_rank_no3.text_count.visibility = View.GONE
        }
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

    override fun onItemClick(item: RankDTO, position: Int) {
        callDonationStatusDialog(item, position.plus(4))
    }

    fun callDonationStatusDialog(item: RankDTO, rank: Int) {
        val dialog = DonationStatusDialog(requireContext(), item, rank)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        dialog.button_ok.setOnClickListener { // OK
            dialog.dismiss()
        }
    }
}

