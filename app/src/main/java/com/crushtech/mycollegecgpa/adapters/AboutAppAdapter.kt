package com.crushtech.mycollegecgpa.adapters

import android.graphics.drawable.AnimationDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getDrawable
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.adapters.AboutAppAdapter.AboutAppViewHolder
import com.crushtech.mycollegecgpa.ui.fragments.AboutAppItems
import kotlinx.android.synthetic.main.about_app_items.view.*


class AboutAppAdapter : RecyclerView.Adapter<AboutAppViewHolder>() {
    private var counter = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AboutAppViewHolder {
        return AboutAppViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.about_app_items, parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: AboutAppViewHolder, position: Int) {
        val items = differ.currentList[position]
        holder.itemView.apply {
            aboutAppTv.text = items.title
            sub_item.text = items.subItem
            ivExpandedAbApp.setImageDrawable(
                getDrawable(
                    context,
                    R.drawable.vector_animation
                )
            )

            setOnClickListener {
                onItemClickListener?.let { click ->
                    items.isExpanded = true
                    if(items.isExpanded && counter % 2 == 0){
                        ivExpandedAbApp.setImageDrawable(
                            getDrawable(context,R.drawable.vector_animation)
                        )
                        val frameAnimation = ivExpandedAbApp.drawable as AnimationDrawable
                        frameAnimation.start()
                        sub_item.visibility = View.VISIBLE
                    }else{
                        ivExpandedAbApp.setImageDrawable(
                            getDrawable(context,R.drawable.vector_animation1)
                        )
                        val frameAnim = ivExpandedAbApp.drawable as AnimationDrawable
                        frameAnim.start()
                        sub_item.visibility = View.GONE
                    }
                    click(items)
                    counter++
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


    inner class AboutAppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}