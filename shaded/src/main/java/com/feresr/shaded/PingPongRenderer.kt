package com.feresr.shaded

import android.graphics.Bitmap
import android.graphics.Rect
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

    private val originalTexture by lazy { placeholders[0]}
    private val textures by lazy { arrayListOf(placeholders[1], placeholders[2]) }
    private val frameBuffers by lazy { Array(2) { FrameBuffer() } }

    private val layer by lazy { Layer() }

    /**
     * Renders all filters to a texture with the dimensions specified in its constructor
     */
    fun render(target: SurfaceView, filters: Collection<Filter>, rect: Rect? = null) {
        layer.bind()
        textures.forEach { it.resize(target.width, target.height) }
        frameBuffers[0].setColorAttachment(textures[1])
        frameBuffers[1].setColorAttachment(textures[0])
        // Set scissor rectangle if provided
        //if (rect != null) glScissor(rect.left, rect.top, rect.width(), rect.height())

        var readFrom: Texture = originalTexture
        readFrom.bind()
        // Render filters
        for ((i, filter) in filters.withIndex()) {
            //glViewport(0, 0, target.width, target.height)
            // Select FBO to render to
            val renderTo = frameBuffers[i % 2]
            renderTo.bind()
            filter.render()
            readFrom = renderTo.colorAttachment!!
            readFrom.bind()
        }

        glViewport(0, 0, target.width, target.height)
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
}
