package com.example.hikingapp.ui.navigation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.example.hikingapp.R

class MapFragment : Fragment() {

    private lateinit var layout: View
    lateinit var touchListener: OnTouchListener
    private lateinit var view: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        layout = inflater.inflate(R.layout.fragment_map, container, false)

        val frameLayout = TouchableWrapper(requireActivity())

//        frameLayout.setBackgroundColor(resources.getColor(android.R.color.transparent))

        (layout as ViewGroup?)!!.addView(
            frameLayout,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        return layout
    }

    fun setListener(listener: OnTouchListener) {
        touchListener = listener
    }

    interface OnTouchListener {
        fun onTouch()
    }

    inner class TouchableWrapper(context: Context) :
        FrameLayout(context) {

        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> touchListener.onTouch()
                MotionEvent.ACTION_UP -> touchListener.onTouch()
            }
            return super.dispatchTouchEvent(event)
        }
    }

}