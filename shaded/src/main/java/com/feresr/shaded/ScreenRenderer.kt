package com.feresr.shaded

import android.content.Context
import android.graphics.Matrix
import android.opengl.GLES10.GL_TEXTURE0
import android.opengl.GLES10.glActiveTexture
import android.opengl.GLES10.glDrawArrays
import android.opengl.GLES20.GL_FRAMEBUFFER
import android.opengl.GLES30
import javax.microedition.khronos.opengles.GL10

internal class ScreenRenderer(private val context: Context) {

    private var program: Int = 0

    fun render(readFromTexture: Int) {
        if (program == 0) {
            program = loadProgram(
                context.resources.openRawResource(R.raw.fragment).reader().readText(),
                context.resources.openRawResource(R.raw.vertexscreen).reader().readText()
            )
        }

        GLES30.glUseProgram(program)

        // Read from
        glActiveTexture(GL_TEXTURE0)
        GLES30.glBindTexture(GL10.GL_TEXTURE_2D, readFromTexture)
        // Draw to the screen (FBO = 0)
        GLES30.glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)

        GLES30.glBindTexture(GL10.GL_TEXTURE_2D, 0)
        GLES30.glUseProgram(0)
    }

    fun setMatrix(matrix: Matrix) {
        val array = FloatArray(8)
        //TODO: matrix.mapPoints(array, TEX_VERTICES)
    }

    fun delete() {
        GLES30.glDeleteProgram(program)
        program = 0
    }
}
