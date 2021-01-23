package com.crushtech.myccga.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.crushtech.myccga.R
import com.crushtech.myccga.databinding.EditTextSemesterBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddSemesterDialogFragment : DialogFragment() {
    private lateinit var binding: EditTextSemesterBinding
    private var positiveListener: ((String) -> Unit)? = null

    fun setPositiveListener(listener: (String) -> Unit) {
        positiveListener = listener
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = EditTextSemesterBinding.inflate(
            LayoutInflater.from(context),
            null, false
        )
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create Semester")
            .setMessage("Enter the semester's name you wish to create")
            .setView(binding.root)
            .setBackground(
                ResourcesCompat.getDrawable(
                    resources, R.drawable.dialog_bg, null
                )
            )
            .setPositiveButton("Create") { _, _ ->
                val semesterName = binding.etAddSemesterName.text.toString()
                positiveListener?.let { yes ->
                    yes(semesterName)

                }
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
    }


}