package com.crushtech.mycollegecgpa.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.crushtech.mycollegecgpa.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.others_layout.*


class AboutAppDialogFragment : DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val aboutAppParent = LayoutInflater.from(requireContext()).inflate(
            R.layout.about_app_layout,
            OthersContainer,
            false
        ) as ConstraintLayout

        aboutAppParent.findViewById<MaterialButton>(R.id.close_about_app).setOnClickListener {
            dialog?.dismiss()
        }
        return MaterialAlertDialogBuilder(requireContext())
            .setView(aboutAppParent)
            .create()
    }
}