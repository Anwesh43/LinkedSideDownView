package com.anwesh.uiprojects.linkedsidedownview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.sidedownview.SideDownView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SideDownView.create(this)
    }
}
