package com.feresr.shaded

import android.graphics.Bitmap
import android.opengl.GLES10.GL_DEPTH_BUFFER_BIT
import android.opengl.GLES10.glClearColor
import android.opengl.GLES10.glGetError
import android.opengl.GLES20
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glViewport
import android.opengl.GLES30.GL_FRAMEBUFFER
import android.opengl.GLES30.glBindFramebuffer
import android.opengl.GLU
import android.view.SurfaceView
import com.feresr.shaded.opengl.FrameBuffer
import com.feresr.shaded.opengl.Texture

/**
 * This class implements ping-pong rendering between textures, starting off
 * from [originalTexture]
 */
internal class PingPongRenderer(private val defaultFilter: Filter) {
    private val placeholders by lazy { Array(3) { Texture() } }

    private val originalTexture by lazy { placeholders[0] }
    private val textures by lazy { arrayListOf(placeholders[1], placeholders[2]) }
    private val frameBuffers by lazy { Array(2) { FrameBuffer() } }

    private val layer by lazy { Layer() }

    /**
     * Renders all filters to a texture with the dimensions specified in its constructor
     */
    fun render(target: SurfaceView, filters: Collection<Filter>) {
        // todo: Duplicating the last render. (just render the last filter to the screen)
        pingPong(target.width, target.height, filters) { lastFbo ->
            //glViewport(0, 0, target.width, target.height)
            // write to screen (fbo: 0)
            glBindFramebuffer(GL_FRAMEBUFFER, 0)
            glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            defaultFilter.render()
            val glError = glGetError()
            if (glError != GLES20.GL_NO_ERROR) {
                throw RuntimeException("Failed to initialise filter: ${GLU.gluErrorString(glError)}");
            }
        }
    }

    private fun pingPong(
        width: Int,
        height: Int,
        filters: Collection<Filter>,
        finally: (FrameBuffer) -> Unit
    ) {
        layer.bind()
        textures.forEach { it.resize(width, height) }
        frameBuffers[0].setColorAttachment(textures[1])
        frameBuffers[1].setColorAttachment(textures[0])
        glViewport(0, 0, width, height)

        var readFrom: Texture = originalTexture
        var renderTo = FrameBuffer(isScreenBuffer = true)
        readFrom.bind()
        // Render filters
        for ((i, filter) in filters.withIndex()) {
            // Select FBO to render to
            renderTo = frameBuffers[i % 2]
            renderTo.bind()
            glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            filter.render()
            readFrom = renderTo.colorAttachment!!
            readFrom.bind()
            //glViewport(0, 0, width, height)
        }
        finally(renderTo)
    }

    fun delete() {
        frameBuffers.forEach { it.delete() }
        textures.forEach { it.delete() }
        originalTexture.delete()
    }

    /**
     * Uploads this bitmap into [originalTexture] (GPU graphics memory)
     * resizes the texture to fit. Performes no changes to the bitmap
     * passed in as a parameter.
     */
    fun setData(bitmap: Bitmap) {
        originalTexture.setData(bitmap)
    }

    fun getBitmap(bitmap: Bitmap, filters: Collection<Filter>) {
        pingPong(bitmap.width, bitmap.height, filters) { frameBuffer ->
            frameBuffer.copyInto(bitmap)
        }
    }
}
