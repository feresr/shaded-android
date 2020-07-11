package com.feresr.shaded

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.feresr.shaded.shaders.FilterBlur
import com.feresr.shaded.shaders.FilterBrightness
import com.feresr.shaded.shaders.FilterContrast
import com.feresr.shaded.shaders.FilterHue
import com.feresr.shaded.shaders.FilterInverse
import com.feresr.shaded.shaders.FilterVignette
import kotlinx.android.synthetic.main.activity_main.clearBitmapButton
import kotlinx.android.synthetic.main.activity_main.result
import kotlinx.android.synthetic.main.activity_main.seekbar
import kotlinx.android.synthetic.main.activity_main.setBitmapButton
import kotlinx.android.synthetic.main.activity_main.surfaceview
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : AppCompatActivity() {

    val contrast = FilterContrast(this, cos(0f))
    val hue = FilterHue(this, sin(0f))
    val inverse = FilterInverse(this, sin(0f))
    val bright = FilterBrightness(this, sin(0f))
    val blur = FilterBlur(this, sin(0f), 0f)
    val vig = FilterVignette(this, FilterVignette.VignetteConfig())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        surfaceview.renderer.addFilter(hue)
        surfaceview.renderer.addFilter(vig)
        surfaceview.renderer.addFilter(contrast)
        surfaceview.renderer.addFilter(blur)
        surfaceview.renderer.addFilter(inverse)
        surfaceview.renderer.addFilter(bright)

        surfaceview.renderer.setBitmap(BitmapFactory.decodeResource(resources, R.drawable.ducks))

        clearBitmapButton.setOnClickListener {
            surfaceview.renderer.removeFilter(vig)
            vig.delete()
        }
        setBitmapButton.setOnClickListener {
            surfaceview.renderer.setBitmap(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.ducks
                )
            )
        }
    }

    override fun onStop() {
        seekbar.setOnSeekBarChangeListener(null)
        surfaceview.onPause()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        surfaceview.onResume()
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                hue.value = sin(progress.toFloat() / 10f)
                contrast.contrast = sin(progress.toFloat() / 20f)
                inverse.alpha = cos(progress.toFloat() / 100f)
                bright.brightness = sin(progress.toFloat() / 100f)
                blur.x = sin(progress.toFloat() / 1000f)
                blur.y = sin(progress.toFloat() / 1000f)
                vig.config = FilterVignette.VignetteConfig(
                    start = sin(progress.toFloat() / 50f),
                    center = .5f to .5f
                )
                Log.i("MainActivity", "onProgressChanged")
                surfaceview.renderer.refresh()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: downscale (better performance on large bitmaps)
                Log.i("MainActivity", "onStartTrackingTouch")
                surfaceview.renderer.downScale(10)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.i("MainActivity", "onStopTrackingTouch1")
                surfaceview.renderer.downScale(1)
                Log.i("MainActivity", "onStopTrackingTouch2")
                surfaceview.renderer.getBitmap { result.setImageBitmap(it) }
                Log.i("MainActivity", "onStopTrackingTouch3")
            }
        })
    }

}
