package com.ados.everybodysingerrematch.page

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ados.everybodysingerrematch.DatabaseHelper
import com.ados.everybodysingerrematch.R
import com.ados.everybodysingerrematch.model.BoardDTO
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.list_item_cheering.view.*
import java.text.SimpleDateFormat

class RecyclerViewAdapterCheering(val items: List<BoardDTO>, var clickListener: OnCheeringItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterCheering.CheeringViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = CheeringViewHolder(parent)

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: CheeringViewHolder, position: Int) {
        holder.initalize(items.get(position),clickListener)

        items[position].let { item ->
            with(holder) {
                if (item.isBlock) {
                    title.text = "차단된 응원글 입니다."
                    content.text = "차단된 응원글 입니다."
                    name.text = "-"
                } else {
                    title.text = item.title
                    content.text = item.content
                    name.text = item.name
                }

                if (item.time != null) {
                    time.text = SimpleDateFormat("yyyy-MM-dd HH:mm").format(item.time)
                }
                likeCount.text = item.likeCount.toString()
                dislikeCount.text = item.dislikeCount.toString()

                if (image != null && item.imageUrl.isNullOrEmpty()) {
                    var imageID = itemView.context.resources.getIdentifier(item.image, "drawable", itemView.context.packageName)

                    if (imageID <= 0) {
                    } else {
                        //image?.setImageResource(imageID)
                        Glide.with(image.context)
                            .asBitmap()
                            .load(imageID) ///feed in path of the image
                            .fitCenter()
                            .into(holder.image)
                    }
                } else {
                    Glide.with(holder.itemView.context).load(item.imageUrl).apply(
                        RequestOptions().centerCrop()).into(image)
                }

            }
        }
    }

    inner class CheeringViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.list_item_cheering, parent, false)) {
        var image = itemView.img_profile
        val title = itemView.text_title
        var content = itemView.text_content
        val name = itemView.text_name
        var time = itemView.text_time
        val likeCount = itemView.text_like_count
        val dislikeCount = itemView.text_dislike_count
        val img_like = itemView.img_like
        val img_dislike = itemView.img_dislike
        val text_dislike_count = itemView.text_dislike_count

        fun initalize(item: BoardDTO, action:OnCheeringItemClickListener) {
            text_dislike_count.visibility = View.GONE
            img_dislike.visibility = View.GONE

            Glide.with(img_like.context)
                .asBitmap()
                .load(R.drawable.like) ///feed in path of the image
                .fitCenter()
                .into(img_like)
            Glide.with(img_dislike.context)
                .asBitmap()
                .load(R.drawable.dislike) ///feed in path of the image
                .fitCenter()
                .into(img_dislike)

            itemView.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }

            // 좋아요, 싫어요는 상세보기에서 하도록 수정
            /*itemView.img_like.setOnClickListener {
                action.onItemClick_like(item, itemView.text_like_count)
            }
            itemView.text_like_count.setOnClickListener {
                action.onItemClick_like(item, itemView.text_like_count)
            }
            itemView.img_dislike.setOnClickListener {
                action.onItemClick_dislike(item, itemView.text_dislike_count)
            }
            itemView.text_dislike_count.setOnClickListener {
                action.onItemClick_dislike(item, itemView.text_dislike_count)
            }*/
        }
    }

}

interface OnCheeringItemClickListener {
    fun onItemClick(item: BoardDTO, position: Int)
    fun onItemClick_like(item: BoardDTO, like: TextView)
    fun onItemClick_dislike(item: BoardDTO, dislike: TextView)
}