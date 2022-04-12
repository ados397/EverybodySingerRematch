package com.ados.everybodysingerrematch.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.everybodysingerrematch.R
import com.ados.everybodysingerrematch.model.EventDTO
import com.ados.everybodysingerrematch.model.NewsDTO
import com.ados.everybodysingerrematch.model.PreferencesDTO
import com.google.firebase.firestore.FirebaseFirestore

import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.admin_dialog.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class LoveGameInputDialog(context: Context) : Dialog(context), View.OnClickListener {

    var firestore : FirebaseFirestore? = null
    private val layout = R.layout.admin_dialog
    var preferencesDTO : PreferencesDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)

        firestore = FirebaseFirestore.getInstance()
        firestore?.collection("preferences")?.document("preferences")?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            preferencesDTO = documentSnapshot?.toObject(PreferencesDTO::class.java)
        }

        init()

        /*button_ticket_morning.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd0900").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 12:00").format(Date())
            val eventDTO = EventDTO("\uD83C\uDF8A설연휴\uD83C\uDF8A 오전 티켓이『7』장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 7, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"모닝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_afternoon.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 18:00").format(Date())
            val eventDTO = EventDTO("\uD83C\uDF8A설연휴\uD83C\uDF8A 정오 티켓이『7』장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 7, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"정오 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_evening.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1800").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val eventDTO = EventDTO("\uD83C\uDF8A설연휴\uD83C\uDF8A 저녁 티켓이『7』장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 7, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"저녁 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_night.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd2200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 02:00").format(Date())
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(limit)
            cal.add(Calendar.DATE, 1)
            val eventDTO = EventDTO("\uD83C\uDF8A설연휴\uD83C\uDF8A 깜짝 티켓은 특별히『9』장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 9, cal.time)

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"깜짝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }*/
        button_ticket_morning.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd0900").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 12:00").format(Date())
            val eventDTO = EventDTO("오전 티켓이 2장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 2, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"모닝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_afternoon.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 18:00").format(Date())
            val eventDTO = EventDTO("정오 티켓이 2장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 2, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"정오 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_evening.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1800").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val eventDTO = EventDTO("저녁 티켓이 2장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 2, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"저녁 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_night.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd2200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 02:00").format(Date())
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(limit)
            cal.add(Calendar.DATE, 1)
            val eventDTO = EventDTO("깜짝 티켓이 2장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 2, cal.time)

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"깜짝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_morning_hottime.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd0900").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 12:00").format(Date())
            val eventDTO = EventDTO("\uD83D\uDD25핫타임\uD83D\uDD25 오전 티켓이 '4'장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 4, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"핫타임 모닝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_afternoon_hottime.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 18:00").format(Date())
            val eventDTO = EventDTO("\uD83D\uDD25핫타임\uD83D\uDD25 정오 티켓이 '4'장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 4, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"핫타임 정오 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_evening_hottime.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd1800").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 23:59:59").format(Date())
            val eventDTO = EventDTO("\uD83D\uDD25핫타임\uD83D\uDD25 저녁 티켓이 '4'장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 4, dateFormat.parse(limit))

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"핫타임 저녁 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }
        button_ticket_night_hottime.setOnClickListener {
            val uid = "t${SimpleDateFormat("yyMMdd2200").format(Date())}"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val limit = SimpleDateFormat("yyyy-MM-dd 02:00").format(Date())
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(limit)
            cal.add(Calendar.DATE, 1)
            val eventDTO = EventDTO("\uD83D\uDD25핫타임\uD83D\uDD25 깜짝 티켓이 '4'장 도착했습니다.\\n지금 수령 하시겠습니까?", uid, 4, cal.time)

            firestore?.collection("event")?.document(uid)?.set(eventDTO)
            Toast.makeText(context,"핫타임 깜짝 티켓 발송 완료.", Toast.LENGTH_SHORT).show()
        }

        button_hottime_start.setOnClickListener {
            var preferencesDTOTemp = preferencesDTO?.copy(
                IntervalTime = 15,
                runHotTime = true,
                rewardCount = 40,
                rewardIntervalTime = 3
            )
            if (preferencesDTOTemp != null) {
                firestore?.collection("preferences")?.document("preferences")?.set(
                    preferencesDTOTemp
                )
                Toast.makeText(context,"핫타임 시작.", Toast.LENGTH_SHORT).show()
            }
        }
        button_hottime_stop.setOnClickListener {
            var preferencesDTOTemp = preferencesDTO?.copy(
                IntervalTime = 60,
                runHotTime = false,
                rewardCount = 20,
                rewardIntervalTime = 5
            )
            if (preferencesDTOTemp != null) {
                firestore?.collection("preferences")?.document("preferences")?.set(
                    preferencesDTOTemp
                )
                Toast.makeText(context,"핫타임 종료.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun init() {
        //button_ok.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        /*when (v.id) {
            R.id.button_ok -> {
                dismiss()
            }
        }*/
    }
}