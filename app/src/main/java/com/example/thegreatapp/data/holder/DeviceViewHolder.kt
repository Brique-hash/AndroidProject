package com.example.thegreatapp.data.holder

import android.view.View
import android.widget.TextView
import com.afollestad.recyclical.ViewHolder
import com.example.thegreatapp.R

class DeviceViewHolder(itemView: View) : ViewHolder(itemView) {
    val name: TextView = itemView.findViewById(R.id.title)
}
