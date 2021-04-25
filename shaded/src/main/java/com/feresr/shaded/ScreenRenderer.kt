package com.feresr.shaded

import android.content.Context
import android.opengl.Matrix
import com.feresr.shaded.opengl.FrameBuffer
import com.feresr.shaded.opengl.Texture
import com.feresr.shaded.shaders.ScreenFilter

@Deprecated("Rendering directly to the screen is no longer supported")
internal class ScreenRenderer(context: Context) {

    private val screenFrameBuffer = FrameBuffer(true)
    private val screenFilter = ScreenFilter(context)


    fun render(
        readFromTexture: Texture,
        screenRatio: Float,
        modelRatio: Float,
        fov: Float,
        cameraX: Float,
        cameraY: Float,
        cameraZ: Float
    ) {

        //Model
        Matrix.setIdentityM(screenFilter.model, 0)
        Matrix.scaleM(screenFilter.model, 0, modelRatio, 1f, 1f)

        //Camera / View
        Matrix.setLookAtM(
            screenFilter.camera,
            0,
            cameraX,
            cameraY,
            cameraZ,
            cameraX,
            cameraY,
            0f,
            0f,
            1.0f,
            0.0f
        )

        //Projection (perspective allows for zooming in/out)
        Matrix.perspectiveM(
            screenFilter.projection,
            0,
            fov,
            screenRatio,
            1f,
            3f
        )

        readFromTexture.bind()
        screenFilter.render()
    }

    fun delete() {
        screenFilter.delete()
        screenFrameBuffer.delete()
    }
}
