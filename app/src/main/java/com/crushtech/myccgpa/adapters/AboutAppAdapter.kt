package com.crushtech.myccgpa.adapters

import android.graphics.drawable.AnimationDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getDrawable
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crushtech.myccgpa.R
import com.crushtech.myccgpa.adapters.AboutAppAdapter.AboutAppViewHolder
import com.crushtech.myccgpa.databinding.AboutAppItemsBinding
import com.crushtech.myccgpa.ui.fragments.AboutAppItems


class AboutAppAdapter : RecyclerView.Adapter<AboutAppViewHolder>() {
    private var counter = 0
    var binding: AboutAppItemsBinding? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AboutAppViewHolder {

        binding = AboutAppItemsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return AboutAppViewHolder(binding!!)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: AboutAppViewHolder, position: Int) {
        val items = differ.currentList[position]
        holder.itemView.apply {
            binding?.let { bind ->
                bind.aboutAppTv.text = items.title
                bind.subItem.text = items.subItem
                bind.ivExpandedAbApp.setImageDrawable(
                    getDrawable(
                        context,
                        R.drawable.vector_animation
                    )
                )
                setOnClickListener {
                    onItemClickListener?.let { click ->
                        items.isExpanded = true
                        if (items.isExpanded && counter % 2 == 0) {
                            bind.ivExpandedAbApp.setImageDrawable(
                                getDrawable(context, R.drawable.vector_animation)
                            )
                            val frameAnimation =
                                bind.ivExpandedAbApp.drawable as AnimationDrawable
                            frameAnimation.start()
                            bind.subItem.visibility = View.VISIBLE
                        } else {
                            bind.ivExpandedAbApp.setImageDrawable(
                                getDrawable(context, R.drawable.vector_animation1)
                            )
                            val frameAnim = bind.ivExpandedAbApp.drawable as AnimationDrawable
                            frameAnim.start()
                            bind.subItem.visibility = View.GONE
                        }
                        click(items)
                        counter++
                    }
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<AboutAppItems>() {
        override fun areItemsTheSame(oldItem: AboutAppItems, newItem: AboutAppItems): Boolean {
            return oldItem.title == oldItem.title
        }

        override fun areContentsTheSame(oldItem: AboutAppItems, newItem: AboutAppItems): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)


    private var onItemClickListener: ((AboutAppItems) -> Unit)? = null

    fun setOnItemClickListener(listener: (AboutAppItems) -> Unit) {
        this.onItemClickListener = listener
    }


    inner class AboutAppViewHolder(binding: AboutAppItemsBinding) :
        RecyclerView.ViewHolder(binding.root)
}