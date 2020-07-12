package com.feresr.shaded

import android.graphics.Bitmap
import android.opengl.GLES30.GL_FRAMEBUFFER
import android.opengl.GLES30.glBindFramebuffer
import android.opengl.GLES30.glViewport
import com.feresr.shaded.opengl.FrameBuffer
import com.feresr.shaded.opengl.Texture

/**
 * This class implements ping-pong rendering between textures for size [width] and [height] starting
 * off from originalTexture (which can be of any size)
 */
internal class PingPongRenderer(private val originalTexture: Texture) {

    private val textures = Array(2) { Texture() }
    private val frameBuffers = Array(2) { FrameBuffer() }
    private var latestFBO: FrameBuffer? = null
    var width = 0
    var height = 0
    private var outputTexture: Texture? = null

    fun initTextures(width: Int, height: Int) {
        textures[0].resize(width, height)
        textures[1].resize(width, height)

        frameBuffers[0].setColorAttachment(textures[1])
        frameBuffers[1].setColorAttachment(textures[0])

        this.width = width
        this.height = height
    }

    fun getOutputTexture(): Texture {
        return outputTexture ?: originalTexture
    }

    /**
     * This method renders all filters to a texture with the dimensions specified in its constructor
     */
    fun render(filters: List<Filter>) {
        glViewport(0, 0, width, height)
        for ((i, filter) in filters.withIndex()) {
            //read from
            (if (i == 0) originalTexture else textures[i % 2]).bind()
            //write to
            latestFBO = frameBuffers[i % 2].apply { bind() }
            filter.render()
        }

        outputTexture = if (filters.isEmpty()) originalTexture else textures[filters.size % 2]
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    fun renderToBitmap(filters: List<Filter>): Bitmap {
        render(filters)
        return latestFBO?.getBitmap()!!
    }

    fun delete() {
        frameBuffers.forEach { it.delete() }
        textures.forEach { it.delete() }
    }
}
