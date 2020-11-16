package com.crushtech.mycollegecgpa.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.crushtech.mycollegecgpa.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class ResetWeightDialogFragment : DialogFragment() {
    private var positiveListener: ((Boolean) -> Unit)? = null

    fun setPositiveListener(listener: (Boolean) -> Unit) {
        positiveListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reset Weight?")
            .setMessage("Do you wish to reset your current weights to default?")
            .setBackground(
                ResourcesCompat.getDrawable(
                    resources, R.drawable.dialog_bg, null
                )
            )
            .setPositiveButton("Yes") { _, _ ->
                positiveListener?.let { reset ->
                    reset(true)
                }
            }

            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
    }
}