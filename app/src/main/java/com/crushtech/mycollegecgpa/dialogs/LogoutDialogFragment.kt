package com.crushtech.mycollegecgpa.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.crushtech.mycollegecgpa.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class LogoutDialogFragment : DialogFragment() {
    private var positiveListener: ((Boolean) -> Unit)? = null

    fun setPositiveListener(listener: (Boolean) -> Unit) {
        positiveListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Log Out?")
            .setMessage("Do you wish to log out?")
            .setBackground(
                ResourcesCompat.getDrawable(
                    resources, R.drawable.dialog_bg, null
                )
            )
            .setPositiveButton("Log out") { _, _ ->
                positiveListener?.let { logOut ->
                    logOut(true)
                }
            }

            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
    }
}