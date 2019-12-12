package com.example.administrator

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class MainAdapter(val context: Context, val urlList : ArrayList<Image>) : RecyclerView.Adapter<MainAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainAdapter.Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.listitem,parent,false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return urlList.size
    }

    override fun onBindViewHolder(holder: MainAdapter.Holder, position: Int) {
        Picasso.get().load(urlList[position].url).into(holder.image)
    }

    inner class Holder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val image = itemView.findViewById<ImageView>(R.id.food_image)

    }
}