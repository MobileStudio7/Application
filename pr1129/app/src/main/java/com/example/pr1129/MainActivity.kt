package com.example.pr1129

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var foodList = arrayListOf<FOOD>(
        FOOD("어묵", "131.12", "17.84","2.95","8.29","100"),
        FOOD("치즈떡볶이", "174.21", "30.76","3.51","4.89","100"),
        FOOD("치즈만두", "190.39", "17.71","9.82","7.78","100"),
        FOOD("카레만두", "143.38", "14.77","5.74","8.15","100"),
        FOOD("튀김만두", "285.45", "22.71","19.39","5.02","100"),
        FOOD("치킨까스","593","44.02","33.92","27.91","200"),
        FOOD("등심돈가스","623.83","38.49","37.54","32.99","200"),
        FOOD("닭강정","310.25","28.42","14.76","15.94","100"),
        FOOD("감자튀김","306.84","37.59","15.74","3.69","100"),
        FOOD("김치볶음밥","754.73","120","20.27","23.07","500"),
        FOOD("불고기덮밥","669.3","102.35","16.16","28.62","500"),
        FOOD("비빔밥","706.62","114.83","18.62","19.92","500"),
        FOOD("치즈바이트 페퍼로니","370","0","0","16.75","125")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val foodAdapter = MainListAdapter(this, foodList)
        mainListView.adapter = foodAdapter
    }
}