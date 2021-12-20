package com.feresr.shaded

import android.graphics.Bitmap
import android.opengl.GLES10.GL_COLOR_BUFFER_BIT
import android.opengl.GLES10.glClearColor
import android.opengl.GLES10.glViewport
import android.opengl.GLES30.GL_FRAMEBUFFER
import android.opengl.GLES30.glBindFramebuffer
import android.opengl.GLES30.glClear
import com.feresr.shaded.opengl.FrameBuffer
import com.feresr.shaded.opengl.Texture

/**
 * This class implements ping-pong rendering between textures, starting off
 * from [originalTexture]
 */
internal class PingPongRenderer(private val defaultFilter: Filter) {

    private val originalTexture: Texture = Texture()

    private val textures = Array(2) { Texture(DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE) }
    private val frameBuffers = Array(2) { FrameBuffer() }

    init {
        frameBuffers[0].setColorAttachment(textures[1])
        frameBuffers[1].setColorAttachment(textures[0])
    }

    /**
     * Renders all filters to a texture with the dimensions specified in its constructor
     */
    fun render(target: Bitmap, filters: List<Filter>): Bitmap {
        textures.forEach { it.resize(target.width, target.height) }
        glViewport(0, 0, target.width, target.height)
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f)

        // Write original image (no filters)
        originalTexture.bind() // read from
        var latestFBO: FrameBuffer = frameBuffers[1].apply { bind() } // write to
        defaultFilter.render()

        // Render filters
        for ((i, filter) in filters.withIndex()) {
            //read from
            textures[i % 2].bind()
            //write to
            latestFBO = frameBuffers[i % 2].apply { bind() }
            glClear(GL_COLOR_BUFFER_BIT)
            filter.render()
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        return latestFBO.copyInto(target)
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

    companion object {
        const val DEFAULT_TEXTURE_SIZE = 10
    }
}
