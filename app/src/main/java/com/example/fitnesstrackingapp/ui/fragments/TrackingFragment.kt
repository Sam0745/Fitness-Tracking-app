package com.example.fitnesstrackingapp.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.fitnesstrackingapp.R
import com.example.fitnesstrackingapp.databinding.FragmentTrackingBinding
import com.example.fitnesstrackingapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.fitnesstrackingapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.fitnesstrackingapp.other.Constants.MAP_ZOOM
import com.example.fitnesstrackingapp.other.Constants.POLYLINE_COLOR
import com.example.fitnesstrackingapp.other.Constants.POLYLINE_WIDTH
import com.example.fitnesstrackingapp.other.TrackingUtility
import com.example.fitnesstrackingapp.services.Polyline
import com.example.fitnesstrackingapp.services.TrackingService
import com.example.fitnesstrackingapp.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.currentCoroutineContext

@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var mBinding: FragmentTrackingBinding
    private var map: GoogleMap? = null
    private var isTracking = false
    private var pathPoint = mutableListOf<Polyline>()
    private var curTimeInMills = 0L


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentTrackingBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.mapView.onCreate(savedInstanceState)
        mBinding.btnToggleRun.setOnClickListener {
           toggleRun()
        }
        mBinding.mapView.getMapAsync {
            map = it
            addAllPolyLines()
        }

        subscribeToObserve()
    }

    private fun subscribeToObserve(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner,Observer{
            pathPoint = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner,Observer{
            curTimeInMills = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMills,true)
            mBinding.tvTimer.text = formattedTime
        })
    }

    private fun toggleRun(){
        if (isTracking){
            sendCommandService(ACTION_PAUSE_SERVICE)
        }else{
            sendCommandService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun moveCameraToUser(){
        if (pathPoint.isNotEmpty() && pathPoint.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoint.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun updateTracking(isTracking:Boolean){
        this.isTracking = isTracking
        if (!isTracking){
            mBinding.btnToggleRun.text = "Start"
            mBinding.btnFinishRun.visibility = View.VISIBLE
        }else{
            mBinding.btnToggleRun.text = "Stop"
            mBinding.btnFinishRun.visibility = View.GONE
        }
    }
    private fun addAllPolyLines(){
        for (polyline in pathPoint){
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline(){
        if (pathPoint.isNotEmpty() && pathPoint.last().size > 1){
            val preLastLatLng = pathPoint.last()[pathPoint.last().size - 2]
            val lastLatLng = pathPoint.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    override fun onResume() {
        super.onResume()
        mBinding.mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mBinding.mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mBinding.mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mBinding.mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mBinding.mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mBinding.mapView?.onSaveInstanceState(outState)
    }


}