package com.ajkerdeal.app.essential.ui.home

import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.ajkerdeal.app.essential.R
import com.google.android.material.textfield.TextInputEditText

class HomeActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        navController = findNavController(R.id.nav_host_fragment)

    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, "অ্যাপটি বন্ধ করতে আবার প্রেস করুন", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000L)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN){
            val view = currentFocus
            if (view is TextInputEditText || view is EditText) {
                if (!isPointInsideView(event.rawX, event.rawY, view)) {
                    view.clearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun isPointInsideView(x: Float, y: Float, view: View): Boolean{
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val viewX = location[0]
        val viewY = location[1]

        // point is inside view bounds
        return ((x > viewX && x < (viewX + view.width)) && (y > viewY && y < (viewY + view.height)))
    }
}
