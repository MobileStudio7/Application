package com.example.administrator

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import org.w3c.dom.Text

class MainListAdapter (val context: Context, val FOODList: ArrayList<FOOD>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(context).inflate(R.layout.main_lv_item, null)

        //val fat: String, val protein : String, val serving_sie : String)

        val foodName = view.findViewById<TextView>(R.id.foodNameText)
        val foodCalrorie = view.findViewById<TextView>(R.id.foodCalrorieText)
        val foodCarbohydrate = view.findViewById<TextView>(R.id.foodCarbohydrateText)
        val foodFat = view.findViewById<TextView>(R.id.foodFatText)
        val foodProtein=view.findViewById<TextView>(R.id.foodProteinText)
        val foodServing_sie = view.findViewById<TextView>(R.id.foodServing_sieText)

        val food = FOODList[position]

        foodName.text = food.name
        foodCalrorie.text = "칼로리 :" + food.calrorie
        foodCarbohydrate.text = "탄수화물 :" +food.carbohydrate
        foodFat.text = "지방 :" + food.fat
        foodProtein.text = "단백질 :" + food.protein
        foodServing_sie.text = "1회 섭취량 :" + food.serving_sie

        return view
    }

    override fun getItem(position: Int): Any {
        return FOODList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return FOODList.size
    }
}