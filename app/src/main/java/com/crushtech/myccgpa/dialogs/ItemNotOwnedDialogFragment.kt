package com.crushtech.myccgpa.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.crushtech.myccgpa.databinding.ItemNotOwnedDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class ItemNotOwnedDialogFragment : DialogFragment() {
    private var deleteCourseListener: ((Boolean) -> Unit)? = null
    private var proceedListener: ((Boolean) -> Unit)? = null
    private lateinit var binding: ItemNotOwnedDialogBinding

    fun setDeleteCourseListener(listener: (Boolean) -> Unit) {
        deleteCourseListener = listener
    }

    fun setProceedListener(listener: (Boolean) -> Unit) {
        proceedListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = ItemNotOwnedDialogBinding.inflate(
            LayoutInflater.from(context),
            null,
            false
        )
        val semesterOwner = arguments?.getString("owner")

        val notOwnerText = binding.itemNotOwnedMessage
        val text = "this semester belongs to $semesterOwner and it's view only"
        notOwnerText.text = text

        binding.proceedBtn.setOnClickListener {
            proceedListener?.let { proceed ->
                proceed(true)
            }
            dialog?.dismiss()
        }
        binding.deleteItemNotOwned.setOnClickListener {
            deleteCourseListener?.let { clicked ->
                clicked(true)
            }
            dialog?.dismiss()
        }
        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }
}