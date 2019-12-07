package com.example.projectresult.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.projectresult.R
import kotlinx.android.synthetic.main.fragment_account2.*


class AccountFragment : Fragment() {

    private lateinit var accountViewModel: AccountViewModel

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
        log_btn.setOnClickListener {
            findNavController().navigate(R.id.to_nestedLogin)
        }
        open_statictic_btn.setOnClickListener {
            findNavController().navigate(R.id.to_statistic)
        }
    }

}