package com.feresr.shaded

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MotionEvent
import android.view.MotionEvent.INVALID_POINTER_ID
import android.view.ScaleGestureDetector
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
import kotlinx.coroutines.job
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val options = BitmapFactory.Options()
        options.inScaled = false

        lifecycleScope.launch {
            shaded.setBitmap(
                BitmapFactory.decodeResource(resources, drawable.square, options),
                true
            )

            image.setImageBitmap(shaded.getBitmap())
        }


        val scaleGestureDetector =
            ScaleGestureDetector(this, object : ScaleGestureDetector.OnScaleGestureListener {
                override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean = true
                override fun onScaleEnd(detector: ScaleGestureDetector?) {}
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    shaded.changeZoomBy(detector.scaleFactor)
                    return true
                }
            })

        var lastTouchX = 0f
        var lastTouchY = 0f
        var activePointerId = 0

        image.setOnTouchListener { v, event ->
            if (event.pointerCount >= 2) scaleGestureDetector.onTouchEvent(event)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    activePointerId = event.getPointerId(0)
                    lastTouchX = event.x
                    lastTouchY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    if (activePointerId != INVALID_POINTER_ID) {
                        if (event.pointerCount == 1) {
                            val (x: Float, y: Float) = event.x to event.y
                            shaded.moveCameraBy((lastTouchX - x), (y - lastTouchY))
                            lastTouchX = x
                            lastTouchY = y
                        }

                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    activePointerId = INVALID_POINTER_ID
                    v.performClick()
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    event.getPointerId(event.actionIndex)
                        .takeIf { it == activePointerId }
                        ?.run {
                            val newPointerIndex = if (event.actionIndex == 0) 1 else 0
                            lastTouchX = event.getX(newPointerIndex)
                            lastTouchY = event.getY(newPointerIndex)
                            activePointerId = event.getPointerId(newPointerIndex)
                        }
                }
            }
            return@setOnTouchListener true
        }

        removeFilter.setOnClickListener {
            filterIndex = 0

            lifecycleScope.launch {
                shaded.clearFilters()
                image.setImageBitmap(shaded.getBitmap())
            }
        }

        changeBitmap.setOnClickListener {
            lifecycleScope.launch {

                shaded.setBitmap(
                    BitmapFactory.decodeResource(
                        resources,
                        bitmaps[currentBitmap % bitmaps.size],
                        options
                    ),
                    true
                )
                currentBitmap++
                image.setImageBitmap(shaded.getBitmap())
            }

        }
        addFilter.setOnClickListener {
            //shaded.clearFilters()
            lifecycleScope.launch {
                shaded.addFilter(filters[filterIndex % filters.size])
                filterIndex++
                image.setImageBitmap(shaded.getBitmap())
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
                    image.setImageBitmap(shaded.getBitmap())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: downscale (better performance on large bitmaps)
                lifecycleScope.launch {
                    shaded.downScale(4)
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                lifecycleScope.launch {
                    shaded.downScale(1)
                    image.setImageBitmap(shaded.getBitmap())
                }
            }
        })
    }


    override fun onDestroy() {
        shaded.dispose()
        super.onDestroy()
    }
}
