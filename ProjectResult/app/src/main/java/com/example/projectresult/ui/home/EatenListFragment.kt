package com.example.projectresult.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectresult.R
import com.example.projectresult.ui.home.model.Model
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_subhome_eaten_list.*
import java.text.SimpleDateFormat
import java.util.*

class EatenListFragment : Fragment(){

    private lateinit var pref : SharedPreferences
    private var realDB = FirebaseDatabase.getInstance().getReference("Users")
    private var user : String = ""
    private lateinit var firebaseRecyclerAdapter : FirebaseRecyclerAdapter<Model,MyViewHolder>
    private lateinit var reView : View
    private val now = Date()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.pref = context.getSharedPreferences("ImageData",0) // 0은 MODE_PRIVATE
        reView = LayoutInflater.from(context).inflate(R.layout.fragment_subhome_eaten_list, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val path = SimpleDateFormat("yyyyMMdd").format(now)
        val ref = realDB.child("송근영").child("image").child(path).ref
        val options = FirebaseRecyclerOptions.Builder<Model>()
            .setQuery(ref,Model::class.java).build()
        val recycle = reView.findViewById<RecyclerView>(R.id.recycle_view)

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Model,MyViewHolder>(options){
            // recycleView list 하나씩 model과 view를 binding라 viewholder를 가져옴
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.recycle_item,parent,false)
                return MyViewHolder(view)
            }
            // 실제로 묶는 연산
            override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: Model) {
                val refid = getRef(position).key.toString()
                ref.child(refid).addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {}
                    override fun onDataChange(p0: DataSnapshot) {
                        Picasso.get().load(model.imageUrl).into(holder.imageView)
                    }
                })
            }
        }

        recycle.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        recycle.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return reView
    }

    class MyViewHolder(itemView : View) :RecyclerView.ViewHolder(itemView){
        val imageView = itemView.findViewById<ImageView>(R.id.food_image)
    }


}