package com.crushtech.mycollegecgpa.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.crushtech.mycollegecgpa.databinding.ViewSnackbarBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.ContentViewCallback

class SimpleCustomSnackbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ContentViewCallback {
    var tvMsg: TextView
    var tvAction: TextView
    var imLeft: ImageView
    var layRoot: MaterialCardView
    private var binding: ViewSnackbarBinding = ViewSnackbarBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        //View.inflate(context, R.layout.view_snackbar, this)
        clipToPadding = false
        this.tvMsg = binding.tvMessage
        this.tvAction = binding.tvAction
        this.imLeft = binding.imActionLeft
        this.layRoot = binding.snackConstraint
    }

    override fun animateContentIn(delay: Int, duration: Int) {
        val scaleX = ObjectAnimator.ofFloat(binding.imActionLeft, View.SCALE_X, 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.imActionLeft, View.SCALE_Y, 0f, 1f)
        val animatorSet = AnimatorSet().apply {
            interpolator = OvershootInterpolator()
            setDuration(500)
            playTogether(scaleX, scaleY)
        }
        animatorSet.start()
    }

    override fun animateContentOut(delay: Int, duration: Int) {
    }
}