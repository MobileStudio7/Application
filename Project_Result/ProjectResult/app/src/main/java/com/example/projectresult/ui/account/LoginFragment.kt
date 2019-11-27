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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_login.*


/**
 * A simple [Fragment] subclass.
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
        var age : String = ""
        var gender : String = ""
        var height : String = ""
        var weight : String = ""
    }
    private var dbAuth = FirebaseAuth.getInstance()
    private var db = FirebaseFirestore.getInstance().collection("UserInfo")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adduser_btn.setOnClickListener {
            email = user_email.text.toString().trim()
            passwd = user_passwd.text.toString().trim()
            name = get_userName.text.toString().trim()
            age = user_age.text.toString().trim()
            if(man.isChecked){ gender = "M" }
            if(woman.isChecked){gender = "W"}
            height = user_height.text.toString().trim()
            weight = user_weight.text.toString().trim()
            dbAuth.createUserWithEmailAndPassword(email,passwd).addOnCompleteListener {
                if(it.isSuccessful){
                        Toast.makeText(this.context, "계정 등록 성공!", Toast.LENGTH_SHORT).show()
                        dataInput()
                    }
                    else{
                        Toast.makeText(this.context, "계정 등록 실패!", Toast.LENGTH_SHORT).show()
                    }
                }
            findNavController().navigate(R.id.to_LoginForm)
        }
    }

    private fun dataInput(){
        val data = hashMapOf(
            "name" to "$name", "age" to "$age", "gender" to "$gender", "height" to "$height", "weight" to "$weight", "email" to "$email"
        )
        db.document("$name").set(data)
    }
}
