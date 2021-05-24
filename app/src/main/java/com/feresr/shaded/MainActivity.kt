package com.feresr.shaded

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.feresr.shaded.R.drawable
import com.feresr.shaded.shaders.FilterBlur
import com.feresr.shaded.shaders.FilterBrightness
import com.feresr.shaded.shaders.FilterContrast
import com.feresr.shaded.shaders.FilterExposure
import com.feresr.shaded.shaders.FilterGrain
import com.feresr.shaded.shaders.FilterHighlightsShadows
import com.feresr.shaded.shaders.FilterHue
import com.feresr.shaded.shaders.FilterInverse
import com.feresr.shaded.shaders.FilterSaturation
import com.feresr.shaded.shaders.FilterTemperature
import com.feresr.shaded.shaders.FilterVibrance
import com.feresr.shaded.shaders.FilterVignette
import kotlinx.android.synthetic.main.activity_main.addFilter
import kotlinx.android.synthetic.main.activity_main.changeBitmap
import kotlinx.android.synthetic.main.activity_main.image
import kotlinx.android.synthetic.main.activity_main.removeFilter
import kotlinx.android.synthetic.main.activity_main.seekbar
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : AppCompatActivity() {

    val contrast = FilterContrast(this)
    val hue = FilterHue(this, sin(0f))
    val inverse = FilterInverse(this, sin(0f))
    val bright = FilterBrightness(this, sin(0f))
    val exposure = FilterExposure(this, sin(0f))
    val temperature = FilterTemperature(this)
    val grain = FilterGrain(this)
    val vib = FilterVibrance(this)
    val highShadows = FilterHighlightsShadows(this)
    val saturation = FilterSaturation(this)
    val blur = FilterBlur(this, sin(0f), 0f)
    val vig = FilterVignette(this, FilterVignette.VignetteConfig())

    private val filters = arrayOf(exposure, blur, grain, vib, highShadows, saturation, bright, vig)
    private val bitmaps = arrayOf(drawable.watch, drawable.tv, drawable.ducks, drawable.square)
    private var currentBitmap = 0
    private var filterIndex = 0

    private val shaded = Shaded(this)
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val options = BitmapFactory.Options()
        options.inScaled = false
        options.inMutable = true

        lifecycleScope.launch {
            bitmap = BitmapFactory.decodeResource(resources, drawable.square, options)
            shaded.setBitmap(bitmap)
            image.setImageBitmap(bitmap)
        }

        removeFilter.setOnClickListener {
            filterIndex = 0

            lifecycleScope.launch {
                shaded.clearFilters()
                shaded.render()
            }
        }

        changeBitmap.setOnClickListener {
            lifecycleScope.launch {

                bitmap = BitmapFactory.decodeResource(
                    resources,
                    bitmaps[currentBitmap % bitmaps.size],
                    options
                )

                shaded.setBitmap(bitmap)

                image.setImageBitmap(bitmap)
                currentBitmap++
                shaded.render()
            }

        }
        addFilter.setOnClickListener {
            //shaded.clearFilters()
            lifecycleScope.launch {
                shaded.addFilter(filters[filterIndex % filters.size])
                filterIndex++
                shaded.render()
            }
        }
    }

    override fun onStop() {
        seekbar.setOnSeekBarChangeListener(null)
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                hue.value = sin(progress.toFloat() / 10f)
                contrast.contrast = progress.toFloat() / 100f
                inverse.alpha = cos(progress.toFloat() / 100f)
                bright.brightness = progress.toFloat() / 100f
                exposure.exposure = progress.toFloat() / 100f
                blur.x = sin(progress.toFloat() / 1000f)
                blur.y = sin(progress.toFloat() / 1000f)
                vig.config = FilterVignette.VignetteConfig(
                    start = sin(progress.toFloat() / 50f),
                    center = .5f to .5f
                )
                temperature.temperature = progress.toFloat() / 100f
                temperature.tint = progress.toFloat() / 100f
                temperature.tint = progress.toFloat() / 100f
                highShadows.highlights = progress.toFloat() / 100f
                highShadows.shadows = progress.toFloat() / 100f
                vib.vibrance = progress.toFloat() / 100f
                saturation.saturation = progress.toFloat() / 100f
                grain.grain = progress.toFloat() / 100f

                lifecycleScope.launch {
                    shaded.render()

                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: downscale (better performance on large bitmaps)
                lifecycleScope.launch {

                    //shaded.downScale(4)
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                lifecycleScope.launch {
                    //shaded.downScale(1)
                    shaded.render()
                }
            }
        })
    }


    override fun onDestroy() {
        shaded.dispose()
        super.onDestroy()
    }
}
