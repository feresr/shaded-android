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
import android.opengl.GLES30.glTexImage2D
import android.opengl.GLES30.glViewport
import javax.microedition.khronos.opengles.GL10.GL_TEXTURE_2D

/**
 * This class implements ping-pong rendering between textures for size [width] and [height] starting
 * off from originalTexture (which can be of any size)
 */
internal class PingPongRenderer(private val originalTexture: Int) {

    private val textBuffer = createVerticesBuffer(TEX_COORDS)
    private val posBuffer = createVerticesBuffer(POS_VERTICES)
    private val textures = createTextures(2)
    private val frameBuffers = IntArray(2).also { glGenFramebuffers(2, it, 0) }
    private var latestFBO = 0
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
            attachTextureToFBO(it[0], textures[1])
            attachTextureToFBO(it[1], textures[0])
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
            filter.render(textBuffer, posBuffer)
            latestFBO = frameBuffers[i % 2]
        }

        outputTexture = if (filters.isEmpty()) originalTexture else textures[filters.size % 2]
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    fun renderToBitmap(filters: List<Filter>): Bitmap {
        render(filters)
        glBindFramebuffer(GL_FRAMEBUFFER, latestFBO)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        glReadPixelsInto(bitmap)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        return bitmap
    }

    fun delete() {
        glDeleteFramebuffers(2, frameBuffers, 0)
        glDeleteTextures(2, textures, 0)
        posBuffer.clear()
        textBuffer.clear()
    }

    companion object {
        init {
            System.loadLibrary("native-lib")
        }

        @JvmStatic
        external fun glReadPixelsInto(srcBitmap: Bitmap)

        @JvmStatic
        external fun loadIntoOpenGl(texture: Int)

        @JvmStatic
        external fun storeBitmap(srcBitmap: Bitmap)

        @JvmStatic
        external fun freeBitmap()

        @JvmStatic
        external fun isBitmapStored(): Boolean

        @JvmStatic
        external fun getBitmapWidth(): Int

        @JvmStatic
        external fun getBitmapHeight(): Int

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
