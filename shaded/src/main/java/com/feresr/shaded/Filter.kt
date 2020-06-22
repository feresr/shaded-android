package com.feresr.shaded

import android.content.Context
import android.opengl.GLES30.GL_FLOAT
import android.opengl.GLES30.GL_TRIANGLE_STRIP
import android.opengl.GLES30.glDeleteProgram
import android.opengl.GLES30.glDisableVertexAttribArray
import android.opengl.GLES30.glDrawArrays
import android.opengl.GLES30.glEnableVertexAttribArray
import android.opengl.GLES30.glGetAttribLocation
import android.opengl.GLES30.glUseProgram
import android.opengl.GLES30.glVertexAttribPointer
import androidx.annotation.RawRes
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLContext


abstract class Filter(val context: Context, @RawRes val shader: Int) {

    protected var program: Int = 0
    private var texCoordHandle = 0
    private var posCoordHandle = 0

    /**
     * Renders the image to the currently bounded FBO
     */
    fun render(textBuffer: FloatBuffer, posBuffer: FloatBuffer) {
        if ((EGLContext.getEGL() as EGL10).eglGetCurrentContext() == EGL10.EGL_NO_CONTEXT) {
            throw IllegalStateException("Current thread has no openGL Context attached")
        }
        glUseProgram(program)
        //Puts texture coordinates data into `texCoordHandle (aka a_textcoord)` for this program
        glVertexAttribPointer(
            texCoordHandle,
            2,
            GL_FLOAT,
            false,
            0,
            textBuffer
        )
        glEnableVertexAttribArray(texCoordHandle)

        //Puts texture coordinates data into `posCoordHandle (aka a_position)` for this program
        glVertexAttribPointer(
            posCoordHandle,
            2,
            GL_FLOAT,
            false,
            0,
            posBuffer
        )
        glEnableVertexAttribArray(posCoordHandle)

        updateUniforms()
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)

        glDisableVertexAttribArray(texCoordHandle)
        glDisableVertexAttribArray(posCoordHandle)
    }

    protected open fun bindUniforms() {
        //no op
    }

    protected open fun updateUniforms() {
        //no op
    }

    fun init() {
        program = loadProgram(
            context.resources.openRawResource(shader).reader().readText(),
            context.resources.openRawResource(R.raw.vertex).reader().readText()
        )
        texCoordHandle = glGetAttribLocation(program, "a_texcoord")
        posCoordHandle = glGetAttribLocation(program, "a_position")
        bindUniforms()
    }

    fun delete() {
        glDeleteProgram(program)
    }
}
