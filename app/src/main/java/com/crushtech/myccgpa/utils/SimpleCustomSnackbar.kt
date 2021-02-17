package com.crushtech.myccgpa.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.crushtech.myccgpa.databinding.CustomViewInflationBinding
import com.google.android.material.snackbar.BaseTransientBottomBar
import timber.log.Timber

class SimpleCustomSnackbar(
    parent: ViewGroup,
    content: SimpleCustomSnackbarView
) : BaseTransientBottomBar<SimpleCustomSnackbar>(parent, content, content) {
    init {
        getView().setBackgroundColor(
            ContextCompat.getColor(
                view.context,
                android.R.color.transparent
            )
        )
        getView().setPadding(0, 0, 0, 0)
    }

    companion object {
        fun make(
            view: View,
            message: String, duration: Int,
            listener: View.OnClickListener?, icon: Int, action_label: String?, bg_color: Int?
        ): SimpleCustomSnackbar? {

            // First we find a suitable parent for our custom view
            val parent = view.findSuitableParent() ?: throw IllegalArgumentException(
                "No suitable parent found from the given view. Please provide a valid view."
            )

            // We inflate our custom view
            try {
                val binding: CustomViewInflationBinding = CustomViewInflationBinding.inflate(
                    LayoutInflater.from(view.context), parent, false
                )
//                val customView = LayoutInflater.from(view.context).inflate(
//                    R.layout.custom_view_inflation,
//                    parent,
//                    false
//                ) as SimpleCustomSnackbarView
                // We create and return our Snackbar
                binding.root.tvMsg.text = message
                // binding.tvMsg.text = message
                action_label?.let {
                    binding.root.tvAction.text = action_label
                    binding.root.tvAction.setOnClickListener {
                        listener?.onClick(binding.root.tvAction)
                        //hide snack bar if action label is clicked
                        binding.root.visibility = View.GONE
                    }
                }
                binding.root.imLeft.setImageResource(icon)
                if (bg_color != null) {
                    binding.root.layRoot.setCardBackgroundColor(bg_color)
                }


                return SimpleCustomSnackbar(
                    parent,
                    binding.root
                ).setDuration(duration)
            } catch (e: Exception) {
                e.message?.let { Timber.tag("exception ").v(it) }
            }

            return null
        }

    }

}
