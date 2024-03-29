package com.example.fitnesstrackingapp.other

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import com.example.fitnesstrackingapp.R
import com.example.fitnesstrackingapp.databinding.MarkerViewBinding
import com.example.fitnesstrackingapp.db.Run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CustomMarkerView(
    val runs: List<Run>,
    c: Context,
    layoutId: Int
) : MarkerView(c, layoutId) {

    private val binding: MarkerViewBinding =
        MarkerViewBinding.inflate(LayoutInflater.from(context), this, true)

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())

    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if (e == null) {
            return
        }
        val curRunId = e.x.toInt()
        val run = runs[curRunId]

        val calender = Calendar.getInstance().apply {
            timeInMillis = run.timeStamp
        }

        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        binding.tvDate.text = dateFormat.format(calender.time)

        val avgSpeed = "${run.avgSpeedInKMH}km/h"
        binding.tvAvgSpeed.text = avgSpeed

        val distanceInKm = "${run.distanceInMeters / 1000f}km"
        binding.tvDistance.text = distanceInKm

        binding.tvDuration.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

        val caloriesBurned = "${run.caloriesBurned}kcal"
        binding.tvCaloriesBurned.text = caloriesBurned

    }
}