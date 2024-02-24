package com.example.fitnesstrackingapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.fitnesstrackingapp.R
import com.example.fitnesstrackingapp.databinding.FragmentStatisticsBinding
import com.example.fitnesstrackingapp.other.CustomMarkerView
import com.example.fitnesstrackingapp.other.TrackingUtility
import com.example.fitnesstrackingapp.ui.viewmodels.MainViewModel
import com.example.fitnesstrackingapp.ui.viewmodels.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment() {


    lateinit var mBinding: FragmentStatisticsBinding
    private val viewModel: StatisticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mBinding = FragmentStatisticsBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObserve()
        setupBarChart()
    }

    private fun setupBarChart() {
        mBinding.barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }

        mBinding.barChart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }

        mBinding.barChart.axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }

        mBinding.barChart.apply {
            description.text = context.getString(R.string.avg_speed_over_time)
            legend.isEnabled = false
        }
    }

    private fun subscribeToObserve() {
        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it)
                mBinding.tvTotalTime.text = totalTimeRun
            }

        })

        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
                val km = it / 1000f
                val totalDistance = round(km * 10f) / 10f
                val totalDistanceString = "${totalDistance}km"
                mBinding.tvTotalDistance.text = totalDistanceString
            }
        })

        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val avgSpeed = round(it * 10f) / 10f
                val avgSpeedString = "${avgSpeed}km/h"
                mBinding.tvAverageSpeed.text = avgSpeedString
            }
        })

        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalCalories = "${it}kcal"
                mBinding.tvTotalCalories.text = totalCalories
            }
        })

        viewModel.runsSortedByDate.observe(viewLifecycleOwner, Observer {
            it?.let {
                val allAvgSpeed = it.indices.map { i -> BarEntry(i.toFloat(), it[i].avgSpeedInKMH) }
                val barDataSet =
                    BarDataSet(allAvgSpeed, getString(R.string.avg_speed_over_time)).apply {
                        valueTextColor = Color.WHITE
                        color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
                    }
                mBinding.barChart.data = BarData(barDataSet)
                mBinding.barChart.marker= CustomMarkerView(it.reversed(),requireContext(),R.layout.marker_view)
                mBinding.barChart.invalidate()
            }

        })
    }


}