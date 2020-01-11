package com.feresr.shaded

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.feresr.shaded.shaders.FilterContrast
import com.feresr.shaded.shaders.FilterHue
import kotlinx.android.synthetic.main.activity_main.result
import kotlinx.android.synthetic.main.activity_main.seekbar
import kotlinx.android.synthetic.main.activity_main.surfaceview
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val contrast = FilterContrast(this, cos(0f))
        val hue = FilterHue(this, sin(0f))

        val renderer = Shaded(this, surfaceview, listOf(contrast, hue))
        renderer.setBitmap(BitmapFactory.decodeResource(resources, R.drawable.ducks), 1)

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                hue.value = sin(progress.toFloat() / 10f)
                contrast.contrast = sin(progress.toFloat() / 20f)
                renderer.requestPreviewRender()
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
    }

    override fun onPause() {
        surfaceview.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        surfaceview.onResume()
    }
}
