package com.example.fitnesstrackingapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.fitnesstrackingapp.R
import com.example.fitnesstrackingapp.databinding.ActivityMainBinding
import com.example.fitnesstrackingapp.databinding.FragmentSetupBinding
import com.example.fitnesstrackingapp.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.fitnesstrackingapp.other.Constants.KEY_NAME
import com.example.fitnesstrackingapp.other.Constants.KEY_WEIGHT
import com.example.fitnesstrackingapp.ui.MainActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment() {

    @Inject
    lateinit var sharedPref: SharedPreferences

    @set:Inject
    var isFirstAppOpen = true

    private lateinit var mBinding:FragmentSetupBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentSetupBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isFirstAppOpen){
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment,true)
                .build()
            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )
        }
        mBinding.tvContinue.setOnClickListener {
            val success = writePersonalDAtaToSharedPref()
            if (success){
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            }else{
                Snackbar.make(requireView(),"Please enter all the fields",Snackbar.LENGTH_SHORT).show()
            }

        }
    }

    private fun writePersonalDAtaToSharedPref():Boolean{
        val name = mBinding.etName.text.toString()
        val weight = mBinding.etWeight.text.toString()
        if (name.isEmpty() || weight.isEmpty()){
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME,name)
            .putFloat(KEY_WEIGHT,weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE,false)
            .apply()
        val toolbarText = "Let's go, $name!"

        val activityBinding = (requireActivity() as? AppCompatActivity)?.let { activity ->
            if (activity is MainActivity) {
                activity.binding
            } else {
                null
            }
        }
        activityBinding?.tvToolbarTitle?.text = toolbarText
        return true

    }

}