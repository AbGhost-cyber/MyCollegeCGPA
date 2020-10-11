package com.crushtech.mycollegecgpa.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.crushtech.mycollegecgpa.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.home_layout.*


class AddOwnerDialogFragment : DialogFragment() {

    private var positiveListener: ((String, Boolean) -> Unit)? = null

    fun setPositiveListener(listener: (String, Boolean) -> Unit) {
        positiveListener = listener
    }

    private var negativeListener: ((Boolean) -> Unit)? = null

    fun setNegativeListener(listener: (Boolean) -> Unit) {
        negativeListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val addOwnerEditText = LayoutInflater.from(requireContext()).inflate(
            R.layout.edit_text_email,
            semesterContainer,
            false
        ) as TextInputLayout

        return MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_add_person)
            .setTitle("Share Semester (View Only)")
            .setBackground(
                ResourcesCompat.getDrawable(
                    resources, R.drawable.dialog_bg, null
                )
            )
            .setMessage(
                "Enter an E-mail of a person you want to share the semester with," +
                        " this person will be able to read but can't modify your semester."
            )
            .setView(addOwnerEditText)
            .setPositiveButton("Add") { _, _ ->
                val email = addOwnerEditText
                    .findViewById<EditText>(R.id.etAddOwnerEmail).text.toString()
                positiveListener?.let { yes ->
                    yes(email, true)

                }

            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                negativeListener?.let { clicked ->
                    clicked(true)
                }
                dialogInterface.cancel()
            }
            .create()
    }


}