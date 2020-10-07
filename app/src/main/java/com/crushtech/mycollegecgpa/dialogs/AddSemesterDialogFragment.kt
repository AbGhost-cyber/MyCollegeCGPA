package com.crushtech.mycollegecgpa.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.crushtech.mycollegecgpa.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.home_layout.*

class AddSemesterDialogFragment : DialogFragment() {

    private var positiveListener: ((String) -> Unit)? = null

    fun setPositiveListener(listener: (String) -> Unit) {
        positiveListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val addSemesterEditText = LayoutInflater.from(requireContext()).inflate(
            R.layout.edit_text_semester,
            semesterContainer,
            false
        ) as TextInputLayout

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create Semester")
            .setMessage("Enter the semester's name you wish to create")
            .setView(addSemesterEditText)
            .setPositiveButton("Create") { _, _ ->
                val semesterName = addSemesterEditText
                    .findViewById<EditText>(R.id.etAddSemesterName).text.toString()
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