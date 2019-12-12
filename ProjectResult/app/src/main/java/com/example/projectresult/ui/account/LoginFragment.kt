package com.example.projectresult.ui.account


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.projectresult.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_login.*


/**
 * 회원가입창
 */
class LoginFragment : Fragment() {
    private lateinit var accountViewModel: AccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        accountViewModel =
            ViewModelProviders.of(this).get(AccountViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_login, container, false)
        return root
    }

    companion object{
        var email : String = ""
        var passwd : String = ""
        var name : String = ""
        var birth : String = ""
        var gender : String = ""
        var height : String = ""
        var weight : String = ""
        var target = ""
    }
    private var dbAuth = FirebaseAuth.getInstance()
    private var realDB = FirebaseDatabase.getInstance().reference
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adduser_btn.setOnClickListener {
            email = input_email.text.toString().trim()
            passwd = input_passwd.text.toString().trim()
            name = user_name.text.toString().trim()
            birth = user_birth.text.toString().trim()
            if (man.isChecked) {
                gender = "M"
            }
            if (woman.isChecked) {
                gender = "W"
            }
            height = input_height.text.toString().trim()
            weight = input_weight.text.toString().trim()
            target = input_target_weight.text.toString().trim()
            dbAuth.createUserWithEmailAndPassword(email, passwd).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this.context, "계정 등록 성공!", Toast.LENGTH_SHORT).show()
                    addUser(name,email,birth,gender, height, weight,target)
                } else {
                    Toast.makeText(this.context, "계정 등록 실패!", Toast.LENGTH_SHORT).show()
                }
            }
            findNavController().navigate(R.id.to_LoginForm)
        }
    }

    private fun addUser(userName : String, userEmail : String, userBirth : String, userSex : String, userHeight : String, userWeight : String, userTarget : String){
        val user = UserModel(userName,userEmail,userBirth,userSex,userHeight,userWeight,userTarget)
        realDB.child("Users").child(name).setValue(user)
    }
}
