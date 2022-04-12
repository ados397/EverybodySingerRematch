package com.ados.everybodysingerrematch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.everybodysingerrematch.model.RankDTO
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.list_item_rank.view.*

class RecyclerViewAdapterImageSelect(val items: List<RankDTO>, var clickListener: OnImageSelectItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterImageSelect.ImageSelectViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ImageSelectViewHolder(parent)

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerViewAdapterImageSelect.ImageSelectViewHolder, position: Int) {
        //view에 onClickListner를 달고, 그 안에서 직접 만든 itemClickListener를 연결시킨다
        //holder.initalize(images.get(position),clickListener)
        holder.initalize(items.get(position),clickListener)

        items[position].let { item ->
        //images[position].let { item ->
        //images.getResourceId(position, -1).let {item ->
            println("position : $position, ${item.toString()}")

            with(holder) {
                //name.text = "${position+1}. ${item.name}"
                //count.text = "득표수:${decimalFormat.format(item.count)}"

                var imageID = itemView.context.resources.getIdentifier(item.image, "drawable", itemView.context.packageName)

                //println("이미지이름 : ${itemView.context.resources.getText(item)}")
                if (image != null && imageID > 0) {
                    //image?.setImageResource(imageID)

                    Glide.with(image.context)
                        .asBitmap()
                        .load(imageID) ///feed in path of the image
                        .fitCenter()
                        .into(holder.image)
                }
                //name.text = names[position]
                name.text = item.name
            }
        }
    }

    inner class ImageSelectViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.list_item_image_select, parent, false)) {
        var image = itemView.img_profile
        var name = itemView.text_name

        fun initalize(item: RankDTO, action:OnImageSelectItemClickListener) {
            itemView.setOnClickListener {
                action.onItemClick(item)
            }
        }
    }
}

interface OnImageSelectItemClickListener {
    fun onItemClick(item: RankDTO)
}

