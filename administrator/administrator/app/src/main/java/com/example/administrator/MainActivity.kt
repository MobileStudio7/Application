package com.example.administrator

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.ListFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    val storage = FirebaseStorage.getInstance()
    var array = ArrayList<Image>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
        findDate()
        val adapter = MainAdapter(this,array)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        recyclerView.adapter = adapter
        */

        val intent = Intent(this,EditImageActivity::class.java)
        intent.putExtra("imageUrl","name")
        startActivity(intent)
    }

    private fun findDate() {
        val now = Date()
        val format = SimpleDateFormat("yyyyMMdd").format(now)
        val listRef = storage.reference.child("files/uid")

    }
}

