package com.feresr.shaded

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MotionEvent
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

    private val filters = arrayOf(contrast, hue, inverse, bright, blur, vig)
    private var filterIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val options = BitmapFactory.Options()
        options.inScaled = false
        surfaceview.setBitmap(BitmapFactory.decodeResource(resources, R.drawable.ducks, options))
        surfaceview.setOnTouchListener { _, event ->
            when(event.action) {
                MotionEvent.ACTION_MOVE-> surfaceview.setZoom(event.rawY / 1000f)
            }
            return@setOnTouchListener true
        }

        clearBitmapButton.setOnClickListener {
            if (filterIndex > 0) {
                filterIndex--
                surfaceview.removeFilter(filters[(filterIndex % filters.size)])
                surfaceview.refresh()
            }
        }

        setBitmapButton.setOnClickListener {
            surfaceview.addFilter(filters[filterIndex % filters.size])
            filterIndex++
            surfaceview.refresh()
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
                surfaceview.refresh()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: downscale (better performance on large bitmaps)
                surfaceview.scale(2)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                surfaceview.scale(1)
                surfaceview.getBitmap { result.setImageBitmap(it) }
            }
        })
    }

}
