package com.example.projectresult.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.projectresult.R
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_schdule.*

class ScheduleFragment : Fragment() {

    private lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var pickDate : String

    companion object{
        var toFragment = DateEatingListFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        scheduleViewModel =
            ViewModelProviders.of(this).get(ScheduleViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_schdule, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calendarView.setOnDateChangeListener{
                view, year, month, dayOfMonth ->
            pickDate = "$year$month$dayOfMonth"
        }
        val bundle : Bundle? = null
        bundle?.putString("date", pickDate)
        toFragment.arguments?.putAll(bundle)
    }
}