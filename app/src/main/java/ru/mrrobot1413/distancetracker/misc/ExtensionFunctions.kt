package ru.mrrobot1413.distancetracker.misc

import android.view.View
import android.widget.Button

object ExtensionFunctions {

    fun View.show(){
        this.visibility = View.VISIBLE
    }

    fun View.hide(){
        this.visibility = View.GONE
    }

    fun Button.enable(){
        this.isEnabled = true
    }

    fun Button.disable(){
        this.isEnabled = false
    }
}