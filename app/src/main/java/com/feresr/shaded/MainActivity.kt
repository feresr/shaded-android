package com.feresr.shaded

import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.feresr.shaded.shaders.FilterBrightness
import com.feresr.shaded.shaders.FilterHue
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : AppCompatActivity() {

    private var timer = 0.0;
    private val surfaceView : GLSurfaceView by lazy { GLSurfaceView(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(surfaceView)

        val filterBrightness = FilterBrightness(this)
        val filterHue = FilterHue(this, 10f)
        val filters = listOf(filterBrightness, filterHue)
        val renderer = Shaded(this, surfaceView, filters)
        renderer.setBitmap(BitmapFactory.decodeResource(resources, R.drawable.duck))
        surfaceView.postDelayed({
            renderer.setBitmap(BitmapFactory.decodeResource(resources, R.drawable.ducks))
        }, 5000)

        fun tock() {
            surfaceView.postDelayed({
                timer += .1f
                filterHue.value = sin(timer).toFloat()
                filterBrightness.brightness = cos(timer).toFloat()
                renderer.updatePreview()
                tock()
            }, 40)

        }
        tock()
    }

    override fun onPause() {
        surfaceView.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        surfaceView.onResume()
    }
}
