package com.feresr.shaded

import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLES30.GL_FRAMEBUFFER
import android.opengl.GLES30.GL_RGBA
import android.opengl.GLES30.GL_UNSIGNED_BYTE
import android.opengl.GLES30.glBindFramebuffer
import android.opengl.GLES30.glBindTexture
import android.opengl.GLES30.glDeleteFramebuffers
import android.opengl.GLES30.glDeleteTextures
import android.opengl.GLES30.glGenFramebuffers
import android.opengl.GLES30.glReadPixels
import android.opengl.GLES30.glTexImage2D
import android.opengl.GLES30.glViewport
import java.nio.IntBuffer
import javax.microedition.khronos.opengles.GL10.GL_TEXTURE_2D

/**
 * This class implements ping-pong rendering between textures for size [width] and [height] starting
 * off from originalTexture (which can be of any size)
 */
internal class PingPongRenderer(private val originalTexture: Int) {

    private val textBuffer = createVerticesBuffer(TEX_COORDS)
    private val posBuffer = createVerticesBuffer(POS_VERTICES)
    private val textures = createTextures(2)
    private val frameBuffers = IntArray(2)
    private var latestFBO = 0
    private var textureCords = createVerticesBuffer(TEX_COORDS)
    private var width = 0
    private var height = 0
    var outputTexture = -1

    fun initTextures(width: Int, height: Int) {
        glBindTexture(GLES30.GL_TEXTURE_2D, textures[0])
        glTexImage2D(
            GLES30.GL_TEXTURE_2D,
            0,
            GL_RGBA,
            width,
            height,
            0,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            null
        )
        glBindTexture(GLES30.GL_TEXTURE_2D, textures[1])
        glTexImage2D(
            GLES30.GL_TEXTURE_2D,
            0,
            GL_RGBA,
            width,
            height,
            0,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            null
        )

        frameBuffers.also {
            glGenFramebuffers(2, it, 0)
            initFrameBufferObject(it[0], textures[1])
            initFrameBufferObject(it[1], textures[0])
        }

        this.width = width
        this.height = height
    }

    /**
     * This method renders all filters to a texture with the dimensions specified in its constructor
     */
    fun render(filters: List<Filter>) {
        glViewport(0, 0, width, height)
        for ((i, filter) in filters.withIndex()) {
            //read from
            glBindTexture(GL_TEXTURE_2D, if (i == 0) originalTexture else textures[i % 2])
            //write to
            glBindFramebuffer(GL_FRAMEBUFFER, frameBuffers[i % 2])
            filter.render(if (i == 0) textureCords else textBuffer, posBuffer)
            latestFBO = frameBuffers[i % 2]
        }

        outputTexture = if (filters.isEmpty()) originalTexture else textures[filters.size % 2]
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    fun renderToBitmap(filters: List<Filter>): Bitmap {
        render(filters)
        glBindFramebuffer(GL_FRAMEBUFFER, latestFBO)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels: IntBuffer = IntBuffer.wrap(IntArray(width))

        for (row in 0 until height) {
            glReadPixels(0, row, width, 1, GL_RGBA, GL_UNSIGNED_BYTE, pixels)
            val array = pixels.array()
            for (i in array.indices) {
                val red = (array[i] shr 0) and 0xff
                val green = (array[i] shr 8) and 0xff
                val blue = (array[i] shr 16) and 0xff
                val alpha = (array[i] shr 24) and 0xff
                array[i] = (alpha shl 24) or (red shl 16) or ((green) shl 8) or (blue)
            }
            bitmap.setPixels(array, 0, width, 0, row, width, 1)
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        return bitmap
    }

    fun delete() {
        glDeleteFramebuffers(2, frameBuffers, 0)
        glDeleteTextures(2, textures, 0)
    }

    companion object {
        private val POS_VERTICES = floatArrayOf(
            -1.0f, -1.0f,   //bottom left
            -1.0f, 1.0f,    //top left
            1.0f, -1.0f,    //bottom right
            1.0f, 1.0f      //top right
        )
        private val TEX_COORDS = floatArrayOf(
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
        )
    }
}
