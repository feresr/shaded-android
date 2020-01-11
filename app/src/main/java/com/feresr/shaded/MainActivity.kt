package com.feresr.shaded

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.feresr.shaded.shaders.FilterBrightness
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

        //val filterBrightness = FilterBrightness(this, cos(0f).toFloat())
        val filterHue = FilterHue(this, sin(0f * 1.2f))

        val renderer = Shaded(this, surfaceview, listOf(filterHue))
        renderer.setBitmap(BitmapFactory.decodeResource(resources, R.drawable.ducks), 1)

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                filterHue.value = sin(progress.toFloat()/10f)
                //filterBrightness.brightness = sin(progress.toFloat() / 20f)
                renderer.requestRender()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                renderer.downScale(20) //optional
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
