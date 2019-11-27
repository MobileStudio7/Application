package com.example.projectresult.ui.schedule

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectresult.R
import com.google.firebase.storage.FirebaseStorage

class DateEatingListFragment : Fragment(){
    private var dbStorage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_datelist, container, false)
       /* val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        val date = arguments?.get("date").toString()
        val ref = dbStorage.getReference(date)
        val uris = ArrayList<FoodItem>()
        uris.add(FoodItem(ref.path.toUri()))

        val adapter = CustomAdapter(uris)
        recyclerView.adapter = adapter*/
        return root
    }


}

