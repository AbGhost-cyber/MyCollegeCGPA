package com.crushtech.myccgpa.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.crushtech.myccgpa.R
import com.crushtech.myccgpa.databinding.EditTextEmailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class AddOwnerDialogFragment : DialogFragment() {
    private lateinit var binding: EditTextEmailBinding
    private var positiveListener: ((String, Boolean) -> Unit)? = null

    fun setPositiveListener(listener: (String, Boolean) -> Unit) {
        positiveListener = listener
    }

    private var negativeListener: ((Boolean) -> Unit)? = null

    fun setNegativeListener(listener: (Boolean) -> Unit) {
        negativeListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = EditTextEmailBinding.inflate(
            LayoutInflater.from(context),
            null, false
        )

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
            .setView(binding.root)
            .setPositiveButton("Add") { _, _ ->
                val email = binding.etAddOwnerEmail.text.toString()
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