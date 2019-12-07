package com.example.projectresult.ui.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.projectresult.R
import kotlinx.android.synthetic.main.fragment_account2.*
import kotlinx.android.synthetic.main.fragment_statistic.*

/**
 * A simple [Fragment] subclass.
 */
class StatisticFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    // 체중, 성분 분석, 목표 칼로리


    ): View? {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_statistic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar.progress = 23

        fab.setOnClickListener {
            progressBar.progress = (progressBar.progress+5) % 100
        }

    }



}


