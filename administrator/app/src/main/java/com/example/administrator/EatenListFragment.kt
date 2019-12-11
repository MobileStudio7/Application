package com.example.administrator


import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.bumptech.glide.request.RequestOptions
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.ValueEventListener
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_subhome_eaten_list.*

class EatenListFragment : Fragment(){

    companion object {
        @JvmStatic
        fun newInstance() =
            EatenListFragment().apply {
                arguments = Bundle().apply {
                    // putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_subhome_eaten_list, container, false)
        return view
    }

    private lateinit var adapter: EatenListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            // columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val items = listOf(
            Uri.parse("https://firebasestorage.googleapis.com/v0/b/test-e80f4.appspot.com/o/images%2F20191108_5847.png?alt=media&token=3409e89b-c2fc-4f88-a00e-e32a19489184")
        )
    /*
        adapter = EatenListAdapter()
        adapter.replaceItems(items)
        recyclerView.adapter = adapter
        */
    }

    inner class EatenListAdapter : RecyclerView.Adapter<EatenListAdapter.ViewHolder>() {

        private var items = listOf<Uri>()


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.eaten_view, parent, false)
            return ViewHolder(view)
        }



        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
        }

        fun replaceItems(items: List<Uri>) {
            this.items = items
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
            LayoutContainer
    }
}