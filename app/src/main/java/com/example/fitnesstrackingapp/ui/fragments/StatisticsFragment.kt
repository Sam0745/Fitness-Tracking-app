package com.example.fitnesstrackingapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.fitnesstrackingapp.R
import com.example.fitnesstrackingapp.ui.viewmodels.MainViewModel
import com.example.fitnesstrackingapp.ui.viewmodels.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint

class StatisticsFragment : Fragment() {

    private val viewModel: StatisticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

}