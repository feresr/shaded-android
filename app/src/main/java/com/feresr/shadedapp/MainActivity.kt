package com.feresr.shadedapp

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.feresr.shaded.Filter
import com.feresr.shaded.R
import com.feresr.shaded.R.drawable.ducks
import com.feresr.shaded.R.drawable.rrr
import com.feresr.shaded.R.drawable.square
import com.feresr.shaded.R.drawable.tv
import com.feresr.shaded.R.drawable.watch
import com.feresr.shaded.Shaded
import com.feresr.shaded.shaders.Direction
import com.feresr.shaded.shaders.FilterBlur
import kotlinx.android.synthetic.main.activity_main.addFilterButton
import kotlinx.android.synthetic.main.activity_main.changeBitmapButton
import kotlinx.android.synthetic.main.activity_main.changeTargetButton
import kotlinx.android.synthetic.main.activity_main.image1
import kotlinx.android.synthetic.main.activity_main.image2
import kotlinx.android.synthetic.main.activity_main.removeFilterButton
import kotlinx.android.synthetic.main.activity_main.seekbar

class MainActivity : AppCompatActivity(), OnSeekBarChangeListener, SurfaceHolder.Callback {

    private val shaded: Shaded = Shaded(this)
    private val blurh = FilterBlur(this, Direction.HORIZONTAL)
    private val blurv = FilterBlur(this, Direction.VERTICAL)

    private val bitmaps = arrayOf(rrr, watch, tv, ducks, square)
    private val filters = mutableListOf<Filter>()
    private var currentBitmap = 0

    private lateinit var selectedTarget: SurfaceView

    private var canvas: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        selectedTarget = image1
    }

    private fun changeBitmap() {
        canvas = decodeSampledBitmapFromResource(
            resources,
            bitmaps[currentBitmap % bitmaps.size],
            selectedTarget.width,
            selectedTarget.height
        )
        currentBitmap++
        shaded.upload(canvas!!)
        shaded.render(
            selectedTarget,
            filters,
            Rect(0, 0, selectedTarget.width, selectedTarget.height)
        )
    }

    override fun onStart() {
        super.onStart()
        removeFilterButton.setOnClickListener {
            filters.clear()
            shaded.render(selectedTarget, emptyList())
        }
        changeBitmapButton.setOnClickListener { changeBitmap() }
        addFilterButton.setOnClickListener {
            filters.add(blurh)
            filters.add(blurv)
            shaded.render(
                selectedTarget,
                filters,
                Rect(0, 0, selectedTarget.width, selectedTarget.height)
            )
        }
        changeTargetButton.setOnClickListener {
            selectedTarget = if (selectedTarget == image1) image2 else image1
            //shaded.setTarget(selectedTarget)
        }
        seekbar.setOnSeekBarChangeListener(this)
    }


    override fun onResume() {
        super.onResume()
        image1.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                //shaded.setTarget(image1)
                //changeBitmap()
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {}
        })
        image2.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                //shaded.setTarget(image2)
                //changeBitmap()
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        shaded.dispose()
        seekbar.setOnSeekBarChangeListener(null)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (canvas == null) return
        blurv.radius = progress.toFloat() / 10f
        val resolution = canvas!!.width.toFloat() to canvas!!.height.toFloat()
        blurv.resolution = resolution
        blurh.radius = progress.toFloat() / 10f
        blurh.resolution = resolution
        shaded.render(
            selectedTarget,
            filters,
            Rect(0, 0, selectedTarget.width, selectedTarget.height)
        )
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

}
