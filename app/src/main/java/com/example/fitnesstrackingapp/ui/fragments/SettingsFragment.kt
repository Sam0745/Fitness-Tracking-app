package com.example.fitnesstrackingapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnesstrackingapp.R
import com.example.fitnesstrackingapp.databinding.FragmentSettingsBinding
import com.example.fitnesstrackingapp.other.Constants.KEY_NAME
import com.example.fitnesstrackingapp.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SettingsFragment : Fragment() {

    lateinit var mBinding : FragmentSettingsBinding
    @Inject
    lateinit var sharePreferences:SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mBinding = FragmentSettingsBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFieldsFromSharedPref()
        mBinding.btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharePref()
            if (success){
                Snackbar.make(view, getString(R.string.saved_changes),Snackbar.LENGTH_LONG).show()
            }else{
                Snackbar.make(view, getString(R.string.please_fill_out_all_the_fields),Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun loadFieldsFromSharedPref(){
        val name = sharePreferences.getString(KEY_NAME,"")
        val weight = sharePreferences.getFloat(KEY_WEIGHT,80f)

        mBinding.etName.setText(name)
        mBinding.etWeight.setText(weight.toString())
    }

    private fun applyChangesToSharePref():Boolean{
        val nameText = mBinding.etName.text.toString()
        val weightText = mBinding.etWeight.text.toString()
        if (nameText.isEmpty() || weightText.isEmpty()){
            return false
        }
        sharePreferences.edit()
            .putString(KEY_NAME,nameText)
            .putFloat(KEY_WEIGHT,weightText.toFloat())
            .apply()
        val toolbarText = "Let's go, $nameText!"
        val activityBinding = (requireActivity() as? AppCompatActivity)?.let { activity ->
            if (activity is com.example.fitnesstrackingapp.ui.MainActivity) {
                activity.binding
            } else {
                null
            }
        }
        activityBinding?.tvToolbarTitle?.text = toolbarText
        return true
    }

}