package com.feresr.shaded

import android.content.Context
import android.opengl.Matrix
import com.feresr.shaded.opengl.FrameBuffer
import com.feresr.shaded.opengl.Texture
import com.feresr.shaded.shaders.ScreenFilter
import kotlin.math.PI
import kotlin.math.atan

internal class ScreenRenderer(context: Context) {

    private val screenFrameBuffer = FrameBuffer(true)
    private val screenFilter = ScreenFilter(context)
    private val snapSensitivity = .5f
    private val cameraDistance = 2f
    private val snapTo = (atan(.5f / cameraDistance) * 180f / PI.toFloat())

    fun render(readFromTexture: Texture, screenRatio: Float, modelRatio: Float, zoom: Float) {
        if (zoom <= 0) throw IllegalArgumentException("Zoom level must be greater than 0")
        //Model
        Matrix.setIdentityM(screenFilter.model, 0)
        Matrix.scaleM(screenFilter.model, 0, modelRatio, 1f, 0f)

        //Camera / View
        Matrix.setLookAtM(
            screenFilter.camera,
            0,
            0f,
            0f,
            cameraDistance,
            0f,
            0f,
            0f,
            0f,
            1.0f,
            0.0f
        )

        //Projection (perspective allows for zooming in/out)
        var fov = snapTo / zoom
        if (fov in (snapTo - snapSensitivity)..(snapTo + snapSensitivity)) fov = snapTo
        Matrix.perspectiveM(
            screenFilter.projection,
            0,
            fov,
            screenRatio,
            1f,    // zNear is measured from the camera
            2f      // zFar is measured from zNear
        )

        readFromTexture.bind()
        screenFilter.render()
    }

    fun delete() {
        screenFilter.delete()
        screenFrameBuffer.delete()
    }
}
