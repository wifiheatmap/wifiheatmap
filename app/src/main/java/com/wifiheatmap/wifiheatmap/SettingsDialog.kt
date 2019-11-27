package com.wifiheatmap.wifiheatmap

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.wifiheatmap.wifiheatmap.databinding.SettingsDialogBinding
import java.text.SimpleDateFormat
import java.util.*

class SettingsDialog : DialogFragment() {

    private lateinit var binding: SettingsDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            binding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.settings_dialog,
                null,
                false
            )

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

            val builder = AlertDialog.Builder(it)
                .setView(binding.root)
                .setTitle("Settings")
                .setPositiveButton(R.string.apply,
                    DialogInterface.OnClickListener { dialog, id ->
                    })
                .setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                    })

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}