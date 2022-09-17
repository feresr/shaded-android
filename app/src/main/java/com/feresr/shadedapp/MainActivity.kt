package com.feresr.shadedapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.feresr.shaded.Filter
import com.feresr.shaded.R
import com.feresr.shaded.R.drawable
import com.feresr.shaded.render
import com.feresr.shaded.shaders.FilterBlur
import com.feresr.shaded.shaders.FilterBrightness
import com.feresr.shaded.shaders.FilterContrast
import com.feresr.shaded.shaders.FilterExposure
import com.feresr.shaded.shaders.FilterFrame
import com.feresr.shaded.shaders.FilterGrain
import com.feresr.shaded.shaders.FilterHSL
import com.feresr.shaded.shaders.FilterHighlightsShadows
import com.feresr.shaded.shaders.FilterHue
import com.feresr.shaded.shaders.FilterInverse
import com.feresr.shaded.shaders.FilterRedBlue
import com.feresr.shaded.shaders.FilterSaturation
import com.feresr.shaded.shaders.FilterTemperature
import com.feresr.shaded.shaders.FilterVibrance
import com.feresr.shaded.shaders.FilterVignette
import com.feresr.shadedapp.MainActivity.ActorMessage.Update
import kotlinx.android.synthetic.main.activity_main.addFilter
import kotlinx.android.synthetic.main.activity_main.changeBitmap
import kotlinx.android.synthetic.main.activity_main.image
import kotlinx.android.synthetic.main.activity_main.luminanceSeekbar
import kotlinx.android.synthetic.main.activity_main.rainbow
import kotlinx.android.synthetic.main.activity_main.removeFilter
import kotlinx.android.synthetic.main.activity_main.saturationSeekbar
import kotlinx.android.synthetic.main.activity_main.seekbar
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : AppCompatActivity() {
    private val frame = FilterFrame(this)
    private val contrast = FilterContrast(this)
    private val hue = FilterHue(this)

    private val inverse = FilterInverse(this)
    private val redblue = FilterRedBlue(this)
    private val bright = FilterBrightness(this)
    private val exposure = FilterExposure(this)
    private val hsl = FilterHSL(this)
    private val temperature = FilterTemperature(this)
    private val grain = FilterGrain(this)
    private val vib = FilterVibrance(this)
    private val highShadows = FilterHighlightsShadows(this)
    private val saturation = FilterSaturation(this)
    private val blur = FilterBlur(this)
    private val vig = FilterVignette(this)

    private val filters = arrayOf(redblue, grain, vib, highShadows, saturation, bright, vig)
    private val appliedFilters = mutableListOf<Filter>()
    private val bitmaps =
        arrayOf(drawable.rrr, drawable.watch, drawable.tv, drawable.ducks, drawable.square)
    private var currentBitmap = 0
    private var filterIndex = 0

    private lateinit var original: Bitmap
    private lateinit var canvas: Bitmap

    sealed class ActorMessage {
        data class Update(val i: Int) : ActorMessage()
        data class Rescale(val i: Int, val progress: Int) : ActorMessage()
    }

    private val hslUniforms = FloatArray(3 * 8) { 0.0f }

    private val channel = Channel<ActorMessage>(Channel.CONFLATED)


    private fun updateFilters(progress: Int) {
        Log.e("progress", progress.toString())
        hue.updateUniforms(sin(progress.toFloat() / 10f))
        contrast.updateUniforms(progress.toFloat() / 100f)
        inverse.updateUniforms(cos(progress.toFloat() / 100f))
        grain.updateUniforms(sin(progress.toFloat() / 100f))
        bright.updateUniforms(progress.toFloat() / 100f)
        exposure.updateUniforms(progress.toFloat() / 100f)
        blur.updateUniforms(
            sin(progress.toFloat() / 1000f),
            sin(progress.toFloat() / 1000f),
        )
        hslUniforms[colorIndexSelected * 3 + 0] = progress.toFloat() / 100f
        hsl.updateUniforms(hslUniforms)
    }


    private var colorIndexSelected = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        removeFilter.setOnClickListener {
            filterIndex = 0
            appliedFilters.clear()
            lifecycleScope.launch {
                //shaded.render(canvas, appliedFilters)
            }
        }

        changeBitmap.setOnClickListener {
            lifecycleScope.launch {
//                original = BitmapFactory.decodeResource(
//                    resources,
//                    bitmaps[currentBitmap % bitmaps.size],
//                    options
//                )
//                shaded.upload(original)
//                canvas = Bitmap.createScaledBitmap(
//                    original, original.width / 2, original.height / 2, true
//                )
//                currentBitmap++
//                shaded.render(canvas, appliedFilters)
            }
        }

        rainbow.children.forEach { it.alpha = 0.5f }
        rainbow.children.forEachIndexed { index, view ->
            view.setOnClickListener {
                colorIndexSelected = index
                rainbow.children.forEach { it.alpha = 0.5f }
                view.alpha = 1.0f
            }
        }

        addFilter.setOnClickListener {
            //shaded.clearFilters()
            lifecycleScope.launch {
                appliedFilters.add(filters[filterIndex % filters.size])
                filterIndex++
                //shaded.render(canvas, appliedFilters)
            }
        }

        lifecycleScope.launch {
            render(this@MainActivity) {
                val options = BitmapFactory.Options()
                options.inScaled = false
                options.inMutable = true
                original = BitmapFactory.decodeResource(resources, drawable.square, options)
                upload(original)
                canvas = Bitmap.createScaledBitmap(
                    original, original.width / 1, original.height / 1, true
                )
                image.setImageBitmap(canvas)
                for (msg in channel) {
                    when (msg) {
                        is Update -> {
                            updateFilters(msg.i)
                            render(canvas, appliedFilters)
                        }
                        is ActorMessage.Rescale -> {
                            val bitmap = if (msg.i > 0) canvas else original
                            updateFilters(msg.progress)
                            render(bitmap, appliedFilters)
                            image.setImageBitmap(bitmap)
                        }
                    }
                    image.invalidate()
                }
            }
        }
    }

    override fun onStop() {
        seekbar.setOnSeekBarChangeListener(null)
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        luminanceSeekbar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    hslUniforms[colorIndexSelected * 3 + 2] = progress.toFloat() / 100f
                    hsl.updateUniforms(hslUniforms)
                    lifecycleScope.launch {
                        //shaded.render(canvas, appliedFilters)
                        image.setImageBitmap(canvas)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )

        saturationSeekbar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    hslUniforms[colorIndexSelected * 3 + 1] = progress.toFloat() / 100f
                    hsl.updateUniforms(hslUniforms)
                    lifecycleScope.launch {
//                        shaded.render(canvas, appliedFilters)
//                        image.setImageBitmap(canvas)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }

            }
        )
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                lifecycleScope.launch {
                    channel.send(ActorMessage.Update(progress))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Optional: downscale (better performance on large bitmaps)
                lifecycleScope.launch {
                    channel.send(ActorMessage.Rescale(1, seekBar.progress))
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                lifecycleScope.launch {
                    channel.send(ActorMessage.Rescale(0, seekBar.progress))
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
