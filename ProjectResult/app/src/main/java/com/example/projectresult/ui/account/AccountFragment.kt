package com.example.projectresult.ui.account

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.projectresult.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_account2.*

// 계정정보를 담고 있는 화면

class AccountFragment : Fragment() {

    private lateinit var accountViewModel: AccountViewModel
    private var realDB = FirebaseDatabase.getInstance().reference
    private lateinit var pref : SharedPreferences
    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.pref = context.getSharedPreferences("ImageData",0) // 0은 MODE_PRIVATE
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        accountViewModel =
            ViewModelProviders.of(this).get(AccountViewModel::class.java)
        var root = inflater.inflate(R.layout.fragment_account2, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var user = pref.getString("current_user", null)
        val name = view.findViewById<TextView>(R.id.user_name)
        val birth = view.findViewById<TextView>(R.id.user_birth)
        val sex = view.findViewById<TextView>(R.id.user_sex)
        val weight = view.findViewById<TextView>(R.id.user_current_weight)
        val target = view.findViewById<TextView>(R.id.user_target_weight)

        if(user == realDB.child("Users").child("email").key){
            name.setText(realDB.child("Users").child("name").key)
            birth.setText(realDB.child("Users").child("birth").key)
            if(realDB.child("Users").child("sex").key == "M"){
                sex.setText("남성")
            }
            else{
                sex.setText("여성")
            }
            weight.setText(realDB.child("Users").child("weight").key)
            target.setText(realDB.child("Users").child("target").key)
        }

        log_btn.setOnClickListener {
            findNavController().navigate(R.id.to_nestedLogin)
        }
        open_statictic_btn.setOnClickListener {
            findNavController().navigate(R.id.to_statistic)
        }
    }

}