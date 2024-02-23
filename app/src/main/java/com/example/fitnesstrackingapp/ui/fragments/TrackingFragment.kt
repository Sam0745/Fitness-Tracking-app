package com.example.fitnesstrackingapp.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract.CalendarEntity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.fitnesstrackingapp.R
import com.example.fitnesstrackingapp.databinding.FragmentTrackingBinding
import com.example.fitnesstrackingapp.db.Run
import com.example.fitnesstrackingapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.fitnesstrackingapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.fitnesstrackingapp.other.Constants.ACTION_STOP_SERVICE
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.currentCoroutineContext
import java.util.Calendar
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var mBinding: FragmentTrackingBinding
    private var map: GoogleMap? = null
    private var isTracking = false
    private var pathPoint = mutableListOf<Polyline>()
    private var curTimeInMills = 0L
    private var menu: Menu? = null
    private var weight = 80f


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentTrackingBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.mapView.onCreate(savedInstanceState)
        mBinding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        mBinding.btnFinishRun.setOnClickListener{
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }
        mBinding.mapView.getMapAsync {
            map = it
            addAllPolyLines()
        }

        subscribeToObserve()
    }

    private fun subscribeToObserve() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoint = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMills = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMills, true)
            mBinding.tvTimer.text = formattedTime
        })
    }

    private fun toggleRun() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (curTimeInMills > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog() {
        val dialog = MaterialAlertDialogBuilder(
            requireContext(),
            com.google.android.material.R.style.MaterialAlertDialog_Material3
        )
            .setTitle("Cancel the Run?")
            .setMessage("Are you sure to cancel the current run and delete all its data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _, _ ->
                stopRun()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
    }

    private fun stopRun() {
        sendCommandService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun moveCameraToUser() {
        if (pathPoint.isNotEmpty() && pathPoint.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoint.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking) {
            mBinding.btnToggleRun.text = "Start"
            mBinding.btnFinishRun.visibility = View.VISIBLE
        } else {
            mBinding.btnToggleRun.text = "Stop"
            menu?.getItem(0)?.isVisible = true
            mBinding.btnFinishRun.visibility = View.GONE
        }
    }
    private fun endRunAndSaveToDb() {
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for (polyline in pathPoint) {
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed =
                round((distanceInMeters / 1000f) / (curTimeInMills / 1000f / 60 / 60) * 10) / 10f
            val dateTimestamp =  Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f)* weight).toInt()
            val run = Run(bmp,dateTimestamp,avgSpeed,distanceInMeters,curTimeInMills,caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                getString(R.string.run_saved_successfully),
                Snackbar.LENGTH_LONG
            ).show()

            stopRun()
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.builder()
        for (polyLine in pathPoint) {
            for (pos in polyLine) {
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mBinding.mapView.width,
                mBinding.mapView.height,
                (mBinding.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun addAllPolyLines() {
        for (polyline in pathPoint) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline() {
        if (pathPoint.isNotEmpty() && pathPoint.last().size > 1) {
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