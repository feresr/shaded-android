package com.feresr.shaded

import android.graphics.BitmapFactory
import android.os.Bundle
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
    val renderer by lazy { Shaded(this, surfaceview, listOf(vig)) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        renderer.setBitmap(BitmapFactory.decodeResource(resources, R.drawable.ducks))

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                hue.value = sin(progress.toFloat() / 10f)
                contrast.contrast = sin(progress.toFloat() / 20f)
                inverse.alpha = cos(progress.toFloat()/100f)
                bright.brightness = sin(progress.toFloat()/100f)
                blur.x = sin(progress.toFloat()/100f)
                blur.y = sin(progress.toFloat()/100f)
                vig.config = FilterVignette.VignetteConfig(
                    start = sin(progress.toFloat()/50f),
                    center = .5f to .5f
                )
                renderer.rerenderFilters()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: downscale (better performance on large bitmaps)
                renderer.downScale(10)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                renderer.downScale(1)
                renderer.getBitmap { result.setImageBitmap(it) }
            }
        })

        clearBitmapButton.setOnClickListener {
            renderer.destroy()
        }
        setBitmapButton.setOnClickListener {
            renderer.setBitmap(BitmapFactory.decodeResource(resources, R.drawable.ducks))
        }
    }

    override fun onStop() {
        surfaceview.onPause()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        surfaceview.onResume()
    }

    override fun onDestroy() {
        renderer.done()
        super.onDestroy()
    }
}
