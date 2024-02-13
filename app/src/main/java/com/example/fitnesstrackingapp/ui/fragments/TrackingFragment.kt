package com.example.fitnesstrackingapp.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.fitnesstrackingapp.R
import com.example.fitnesstrackingapp.databinding.FragmentTrackingBinding
import com.example.fitnesstrackingapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.fitnesstrackingapp.services.TrackingService
import com.example.fitnesstrackingapp.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.GoogleMap
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var mBinding: FragmentTrackingBinding
    private var map: GoogleMap? = null


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
            sendCommandService(ACTION_START_OR_RESUME_SERVICE)
        }
        mBinding.mapView.getMapAsync {
            map = it
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