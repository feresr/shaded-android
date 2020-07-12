package com.feresr.shaded

import android.content.Context
import com.feresr.shaded.opengl.FrameBuffer
import com.feresr.shaded.opengl.Texture
import com.feresr.shaded.shaders.ScreenFilter

internal class ScreenRenderer(context: Context) {

    private val screenFrameBuffer = FrameBuffer(true)
    private val screenFilter = ScreenFilter(context)

    fun render(readFromTexture: Texture, screenRatio: Float, modelRatio: Float) {
        screenFilter.model.loadIdentity()
        screenFilter.model.scale(modelRatio, 1f, 1f)  //scale quad to picture aspect-ratio
        screenFilter.projection.loadIdentity()
        screenFilter.projection.scale(screenRatio, 1f, 1f) //scale quad to view port aspect-ratio
        readFromTexture.bind()
        screenFilter.render()
    }

    fun delete() {
        screenFilter.delete()
        screenFrameBuffer.delete()
    }
}
