package com.example.projectresult.ui.account


import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.projectresult.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_account.*

/**
 * A simple [Fragment] subclass.
 */
class AccountFragment2 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    companion object{
        lateinit var email : String
        lateinit var passwd : String
    }
    private var dbAuth = FirebaseAuth.getInstance()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        login_btn.setOnClickListener {
            email = email_input.text.toString().trim()
            passwd = passwd_input.text.toString().trim()

            if(email == null){
                Toast.makeText(this.context, "email을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            }
            if(passwd == null){
                Toast.makeText(this.context, "password를 입력해 주세요.", Toast.LENGTH_SHORT).show()
            }
            if(email != null && passwd != null){
                dbAuth.signInWithEmailAndPassword(email,passwd).addOnCompleteListener(activity!!) { task ->
                 if(task.isSuccessful){
                        findNavController().navigate(R.id.to_navAccount)
                    }
                    else{
                     Toast.makeText(this.context, "계정 또는 비밀번호가 맞지 않습니다!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        signup_btn.setOnClickListener {
            findNavController().navigate(R.id.to_addUser)
        }
    }

}
