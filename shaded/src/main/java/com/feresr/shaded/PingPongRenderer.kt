package com.feresr.shaded

import android.graphics.Bitmap
import android.opengl.GLES10.glViewport
import android.opengl.GLES30.GL_FRAMEBUFFER
import android.opengl.GLES30.glBindFramebuffer
import com.feresr.shaded.opengl.FrameBuffer
import com.feresr.shaded.opengl.Texture

/**
 * This class implements ping-pong rendering between textures for size [width] and [height] starting
 * off from originalTexture (which can be of any size)
 */
internal class PingPongRenderer(
    private val defaultFilter: Filter,
    private val originalTexture: Texture
) {

    private val textures = Array(2) { Texture() }
    private val frameBuffers = Array(2) { FrameBuffer() }
    private var width = 0
    private var height = 0

    fun resize(width: Int, height: Int) {
        textures[0].resize(width, height)
        textures[1].resize(width, height)

        frameBuffers[0].setColorAttachment(textures[1])
        frameBuffers[1].setColorAttachment(textures[0])

        this.width = width
        this.height = height
    }

    /**
     * Renders all filters to a texture with the dimensions specified in its constructor
     */
    fun renderToBitmap(filters: List<Filter>): Bitmap {
        glViewport(0, 0, width, height)

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
            filter.render()
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        return latestFBO.getBitmap()
    }

    fun delete() {
        frameBuffers.forEach { it.delete() }
        textures.forEach { it.delete() }
    }
}
