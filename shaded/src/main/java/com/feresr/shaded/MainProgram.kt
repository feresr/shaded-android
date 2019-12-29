package com.feresr.shaded

import android.content.Context
import android.opengl.GLES30
import javax.microedition.khronos.opengles.GL10

internal class MainProgram(private val context: Context) {

    private val transformedPosVertices = createVerticesBuffer(POS_VERTICES)
    private var transformedTextureCords = createVerticesBuffer(TEX_VERTICES)

    private var program: Int = 0
    private var posCoordHandle = 0
    private var texCoordHandle = 0

    fun init() {
        if (program != 0) return
        program = loadProgram(
            context.resources.openRawResource(R.raw.fragment).reader().readText(),
            context.resources.openRawResource(R.raw.vertex).reader().readText()
        )
        GLES30.glUseProgram(program)
        texCoordHandle = GLES30.glGetAttribLocation(program, "a_texcoord")
        posCoordHandle = GLES30.glGetAttribLocation(program, "a_position")
    }

    fun render(texture: Int) {
        GLES30.glUseProgram(program)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)

        GLES30.glVertexAttribPointer(
            texCoordHandle,
            2,
            GLES30.GL_FLOAT,
            true,
            0,
            transformedTextureCords
        )
        GLES30.glEnableVertexAttribArray(texCoordHandle)
        GLES30.glVertexAttribPointer(
            posCoordHandle,
            2,
            GLES30.GL_FLOAT,
            true,
            0,
            transformedPosVertices
        )
        GLES30.glEnableVertexAttribArray(posCoordHandle)
        GLES30.glBindTexture(GL10.GL_TEXTURE_2D, texture) //read from
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
    }

    companion object {
        private val TEX_VERTICES = floatArrayOf(
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
        )
        private val POS_VERTICES = floatArrayOf(
            -1.0f, -1.0f,   //bottom left
            -1.0f, 1.0f,    //top left
            1.0f, -1.0f,    //bottom right
            1.0f, 1.0f      //top right
        )
    }
}
