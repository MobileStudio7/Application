package com.example.projectresult.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectresult.R

class CustomAdapter(val imageUri : ArrayList<FoodItem>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>(){

    override fun getItemCount(): Int {
        return imageUri.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val foodImageUri : FoodItem = imageUri[position]

        //holder.itemView = foodImageUri
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.fragment_subhome_eaten_list, parent, false)
        return ViewHolder(v)
    }

    class ViewHolder(itemVIew : View) : RecyclerView.ViewHolder(itemVIew){
        val imageView = itemView.findViewById(R.id.food_image) as ImageView
    }

}