package com.feresr.shaded

import android.content.Context
import android.opengl.Matrix
import com.feresr.shaded.opengl.FrameBuffer
import com.feresr.shaded.opengl.Texture
import com.feresr.shaded.shaders.ScreenFilter

internal class ScreenRenderer(context: Context) {

    private val screenFrameBuffer = FrameBuffer(true)
    private val screenFilter = ScreenFilter(context)
    private val snapSensitivity = 4f
    private val snapTo = 45f

    fun render(readFromTexture: Texture, screenRatio: Float, modelRatio: Float, zoom: Float) {
        //Model
        Matrix.setIdentityM(screenFilter.model, 0)
        Matrix.scaleM(screenFilter.model, 0, modelRatio, 1f, 0f)

        //Camera / View
        Matrix.setLookAtM(screenFilter.camera, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        //Projection (perspective allows for zooming in/out)
        var fov = (zoom * snapTo).coerceIn(10f, 90f)
        if (fov in (snapTo - snapSensitivity)..(snapTo + snapSensitivity)) fov = snapTo
        Matrix.perspectiveM(
            screenFilter.projection,
            0,
            fov,
            screenRatio,
            1f,
            10f
        )

        readFromTexture.bind()
        screenFilter.render()
    }

    fun delete() {
        screenFilter.delete()
        screenFrameBuffer.delete()
    }
}
