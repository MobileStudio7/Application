package com.example.administrator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.edit_image_activity.*

class EditImageActivity : Activity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_image_activity)

        search_btn.setOnClickListener{
            val intent = Intent(this,FoodListActivity::class.java)
            startActivity(intent)
        }
    }
}