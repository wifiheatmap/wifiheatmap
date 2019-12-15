package com.wifiheatmap.wifiheatmap

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.wifiheatmap.wifiheatmap.databinding.SettingsDialogBinding
import java.text.SimpleDateFormat
import java.util.*

class SettingsDialog : DialogFragment() {

    private lateinit var binding: SettingsDialogBinding
    private lateinit var mapsViewModel: MapsViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        mapsViewModel = ViewModelProviders.of(requireActivity()).get(MapsViewModel::class.java)

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.settings_dialog,
            null,
            false
        )

        binding.toggleDarkModeSwitch.isChecked = mapsViewModel.isDarkModeEnabled.value!!
        binding.toggleColorBlindSwitch.isChecked = mapsViewModel.isColorBlindModeEnabled.value!!
        binding.startDate.setText(mapsViewModel.startDate.value)
        binding.endDate.setText(mapsViewModel.endDate.value)
        val progressAmount = ((mapsViewModel.tileSize.value ?: 0.5) * 100.0).toInt()
        binding.tileSizeSlider.progress = progressAmount

        binding.startDate.setOnClickListener {
            context?.let { context ->
                val calendar = Calendar.getInstance()
                val datePicker = DatePickerDialog(context)
                datePicker.setOnDateSetListener { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                    binding.startDate.setText(sdf.format(calendar.time))
                }
                datePicker.show()
            }
        }

        binding.endDate.setOnClickListener {
            context?.let { context ->
                val calendar = Calendar.getInstance()
                val datePicker = DatePickerDialog(context)
                datePicker.setOnDateSetListener { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                    binding.endDate.setText(sdf.format(calendar.time))
                }
                datePicker.show()
            }
        }

        return activity?.let {
            val builder = AlertDialog.Builder(it)
                .setView(binding.root)
                .setTitle("Settings")
                .setPositiveButton(R.string.apply) { _, _ ->
                    mapsViewModel.isDarkModeEnabled.value =
                        binding.toggleDarkModeSwitch.isChecked
                    mapsViewModel.isColorBlindModeEnabled.value =
                        binding.toggleColorBlindSwitch.isChecked
                    mapsViewModel.startDate.value =
                        binding.startDate.text.toString()
                    mapsViewModel.endDate.value =
                        binding.endDate.text.toString()
                    mapsViewModel.tileSize.value = binding.tileSizeSlider.progress.toDouble() / 100.0
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}