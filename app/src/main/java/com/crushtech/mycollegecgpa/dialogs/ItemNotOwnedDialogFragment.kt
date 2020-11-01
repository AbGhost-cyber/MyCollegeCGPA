package com.crushtech.mycollegecgpa.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.crushtech.mycollegecgpa.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.home_layout.*


class ItemNotOwnedDialogFragment : DialogFragment() {
    private var positiveListener: ((Boolean) -> Unit)? = null

    fun setPositiveListener(listener: (Boolean) -> Unit) {
        positiveListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val semesterParent = LayoutInflater.from(requireContext()).inflate(
            R.layout.item_not_owned_dialog,
            semesterContainer,
            false
        ) as ConstraintLayout
        val semesterOwner = arguments?.getString("owner")

        val notOwnerText = semesterParent.findViewById<TextView>(R.id.item_not_owned_message)
        val text = "this semester belongs to $semesterOwner and it's view only"
        notOwnerText.text = text

        semesterParent.findViewById<MaterialButton>(R.id.close_ino_bg).setOnClickListener {
            dialog?.dismiss()
        }
        semesterParent.findViewById<TextView>(R.id.delete_item_not_owned).setOnClickListener {
            positiveListener?.let { clicked ->
                clicked(true)
            }
            dialog?.dismiss()
        }
        return MaterialAlertDialogBuilder(requireContext())
            .setView(semesterParent)
            .create()
    }
}