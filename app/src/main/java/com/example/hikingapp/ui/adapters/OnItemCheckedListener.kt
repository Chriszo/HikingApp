package com.example.hikingapp.ui.adapters

import android.os.Bundle

interface OnItemCheckedListener {
    fun onItemChecked(position: Int)
    fun onItemUnchecked(position: Int)
}