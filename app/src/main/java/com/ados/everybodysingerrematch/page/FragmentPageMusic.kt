package com.ados.everybodysingerrematch.page


import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.everybodysingerrematch.MainActivity
import com.ados.everybodysingerrematch.R
import com.ados.everybodysingerrematch.model.ItemList
import com.ados.everybodysingerrematch.model.MovieDTO
import com.ados.everybodysingerrematch.model.RankDTO
import com.ados.everybodysingerrematch.model.YoutubeApi
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.gson.GsonBuilder
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.android.synthetic.main.fragment_fragment_page_music.*
import okhttp3.*
import java.io.IOException


class FragmentPageMusic : Fragment(), OnMusicItemClickListener {

    var firestore : FirebaseFirestore? = null
    private var movieList : ArrayList<ItemList> = arrayListOf()
    lateinit var recyclerView : RecyclerView
    var mPlayer: YouTubePlayer? = null
    var curVideoId: String? = null
    var youtubeApi = YoutubeApi()
    var jsonbody: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_fragment_page_music, container, false)

        var rootView = inflater.inflate(R.layout.fragment_fragment_page_music, container, false)
        recyclerView = rootView.findViewById(R.id.recyclerview_music!!)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        /*firestore = FirebaseFirestore.getInstance()
        firestore?.collection("people")?.orderBy("count", Query.Direction.DESCENDING)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            people.clear()
            if (querySnapshot == null) return@addSnapshotListener

            // document ????????? ??????
            for (snapshot in querySnapshot) {
                var person = snapshot.toObject(MovieDTO::class.java)!!
                people.add(person)
            }
            recyclerView.adapter =
                RecyclerViewAdapterMusic(people, this)
        }*/

        firestore = FirebaseFirestore.getInstance()
        firestore?.collection("json")?.document("popular_list")?.get()?.addOnCompleteListener { task ->
            if(task.isSuccessful){
                jsonbody = task.result!!["body"].toString()
                loadMovieList(youtubeApi)
            }
        }
        /*firestore?.collection("preferences")?.document("youtubeapi")?.get()?.addOnCompleteListener { task ->
            if(task.isSuccessful){


                if (task.result!!["q"] != null) youtubeApi.q = task.result!!["q"].toString()
                if (task.result!!["part"] != null) youtubeApi.part = task.result!!["part"].toString()
                if (task.result!!["key"] != null) youtubeApi.key = task.result!!["key"].toString()
                if (task.result!!["type"] != null) youtubeApi.type = task.result!!["type"].toString()
                if (task.result!!["maxResults"] != null) youtubeApi.maxResults = task.result!!["maxResults"].toString()
                if (task.result!!["videoDuration"] != null) youtubeApi.videoDuration = task.result!!["videoDuration"].toString()
                if (task.result!!["keyword"] != null) youtubeApi.keyword = task.result!!["keyword"].toString()

                val url = youtubeApi.getUrl()
                println(url)

                loadMovieList(youtubeApi)
            }
        }*/

        return rootView
    }

    fun setAdapter() {
        recyclerView.adapter = RecyclerViewAdapterMusic(movieList, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //tv_fragment_name.text = "????????????"

        getActivity()?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        player_view.getPlayerUiController().showFullscreenButton(true)
        player_view.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(@NonNull youTubePlayer: YouTubePlayer) {
                mPlayer = youTubePlayer
                val videoId = "uAHaUXV6_vE"
                youTubePlayer.cueVideo(videoId, 0f)

                curVideoId = ""
            }
        })

        player_view.getPlayerUiController().setFullScreenButtonClickListener(View.OnClickListener {
            if (player_view.isFullScreen()) {
                player_view.exitFullScreen()
                getActivity()?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                // Show Tabs
                (activity as MainActivity?)!!.showMainCtrl(true)
                //searchCtrlShow(true)

                //?????? ???????????? ??????
                getActivity()?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

            } else {
                player_view.enterFullScreen()
                getActivity()?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
                // Hide Tabs
                (activity as MainActivity?)!!.showMainCtrl(false)
                //searchCtrlShow(false)

                // ?????? ?????? ????????? ?????? ?????? ?????? ??????
                getActivity()?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }
        })

        button_search.setOnClickListener {
            youtubeApi.q = edit_search_movie.text.toString()
            loadMovieList(youtubeApi)
        }

        edit_search_movie.setOnKeyListener { v, keyCode, event ->
            if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.action == KeyEvent.ACTION_DOWN)) {
                youtubeApi.q = edit_search_movie.text.toString()
                loadMovieList(youtubeApi)
            }
            false
        }

        //button_insertMove.visibility = View.GONE // ???????????????
        button_insertMove.setOnClickListener {
            Toast.makeText(getActivity(),"????????? ??????", Toast.LENGTH_SHORT).show()
            insertMovieList()
        }
    }

    override fun onItemClick(item: ItemList, position: Int) {
        if (curVideoId == item.id.videoId) { // ????????? ????????? ??? ??? ??? ???????????? ???????????? ??????
            curVideoId = ""
            player_view.visibility = View.GONE
        } else {
            curVideoId = item.id.videoId
            mPlayer?.cueVideo(item.id.videoId.toString(), 0f)
            player_view.visibility = View.VISIBLE
        }
    }

    fun insertMovieList() {
        var api = YoutubeApi()
        //api.order = "viewCount"
        var fullData : MovieDTO? = null
        var itemList : ArrayList<ItemList> = arrayListOf()
        var people : ArrayList<RankDTO> = arrayListOf()
        firestore?.collection("people")?.orderBy("count", Query.Direction.DESCENDING)?.limit(10)?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                people.add(person)
            }

            people.add(RankDTO("","???????????????",0,"",0,0,0,0,null))
            var i = 10
            for (person in people) {
                if (i == 0)
                    i = 30

                api.q = person.name
                api.channelId = null
                //api.q += "??????"
                /*if (person.name.equals("?????????"))
                    api.q += "tv"*/
                when (person.name) {
                    "?????????" -> {
                        //api.q += " ?????? ??????"
                        api.channelId = "UC3WZlO2Zl8NE1yIUgtwUtQw"
                    }
                    "?????????" -> {
                        //api.q += " ??????"
                        api.channelId = "UC4UnP3v-iaFaLdtKwp84Pmw"
                    }
                    "?????????" -> api.q += " HEEJAE"
                    "?????????" -> {
                        api.q += "tv"
                        api.channelId = "UCrLQ0ovys23H9xBV6U-Sd4A"
                    }
                    "?????????" -> api.q += "tv"
                    "?????????" -> api.q += " ?????????"
                    "??????" -> api.q += "??? ??????tv"
                    "??????" -> api.q += " ????????? ??????"
                    "?????????" -> api.q += " ????????? ??????"
                    "?????????" -> api.q += " tv"
                    "?????????" -> api.q += " tv"
                    "?????????" -> {
                        //api.q += " tv"
                        api.channelId = "UCgLn4rH3Ey9OWSd88-HMUyQtoRegex"
                    }
                    else -> " ??????"
                }

                api.maxResults = i.toString()

                val url = api.getUrl()
                println(url)
                val request = Request.Builder().url(url).build()

                var client = OkHttpClient()
                client.newCall(request).enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {

                    }

                    override fun onResponse(call: Call, response: Response) {
                        val body = response?.body?.string()
                        val gson = GsonBuilder().create()

                        //println(body)

                        fullData = gson.fromJson(body, MovieDTO::class.java)
                        for (it in fullData!!.items) {
                            itemList.add(it)
                        }

                        println("????????? ??????")
                        fullData?.items = itemList
                        var body2 = gson.toJson(fullData)

                        println(itemList)
                        println(fullData)
                        println(body2)

                        val data = hashMapOf("body" to body2)
                        firestore?.collection("json")?.document("popular_list")?.set(data, SetOptions.merge())

                        getActivity()?.runOnUiThread {

                        }


                    }
                })

                SystemClock.sleep(1000)
                i--;
            }

            Toast.makeText(getActivity(),"????????? ??????", Toast.LENGTH_SHORT).show()
        }
            ?.addOnFailureListener { exception ->

            }


        /*var names = arrayOf("?????????","??????","?????????","?????????","?????????","?????????","?????????","?????????","?????????","?????????","???????????????")
        //var names = arrayOf("?????????","??????")
        var i = 10

        for (name in names) {
            if (i == 0)
                i = 10

            api.q = name
            api.maxResults = i.toString()

            val url = api.getUrl()
            println(url)
            val request = Request.Builder().url(url).build()

            var client = OkHttpClient()
            client.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {

                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response?.body?.string()
                    val gson = GsonBuilder().create()

                    //println(body)

                    fullData = gson.fromJson(body, MovieDTO::class.java)
                    for (it in fullData!!.items) {
                        itemList.add(it)
                    }

                    println("????????? ??????")
                    fullData?.items = itemList
                    val gson2 = GsonBuilder().create()
                    var body2 = gson2.toJson(fullData)

                    println(itemList)
                    println(fullData)
                    println(body2)

                    val data = hashMapOf("body" to body2)
                    firestore?.collection("json")?.document("popular_list2")?.set(data, SetOptions.merge())

                    getActivity()?.runOnUiThread {

                    }


                }
            })

            SystemClock.sleep(1000)
            i--;
        }*/



    }

    fun loadMovieList(youtubeApi: YoutubeApi) {
        val gson = GsonBuilder().create()
        val fullData = gson.fromJson(jsonbody, MovieDTO::class.java)
        movieList = fullData.items as ArrayList<ItemList>

        getActivity()?.runOnUiThread {
            setAdapter()
        }

        /*val url = youtubeApi.getUrl()
        val request = Request.Builder().url(url).build()

        var client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                val body = response?.body?.string()
                val gson = GsonBuilder().create()

                val fullData = gson.fromJson(jsonbody, MovieDTO::class.java)
                movieList = fullData.items as ArrayList<ItemList>

                getActivity()?.runOnUiThread {
                    setAdapter()
                }

            }
        })*/
    }

    fun searchCtrlShow(show: Boolean) {
        if (show) {
            layout_search_movie.visibility = View.VISIBLE
        } else {
            layout_search_movie.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        // ??????
    }

    override fun onPause() {
        super.onPause()
        // ??????

        mPlayer?.pause()
    }
}
