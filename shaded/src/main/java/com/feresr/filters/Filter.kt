package com.feresr.filters

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import androidx.annotation.RawRes
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLContext


abstract class Filter(val context: Context, @RawRes val shader: Int) {

    protected var program: Int = 0
    private var texCoordHandle = 0
    private var posCoordHandle = 0

    fun init() {
        program = loadProgram(
            context.resources.openRawResource(shader).reader().readText(),
            context.resources.openRawResource(R.raw.vertex).reader().readText()
        )
        GLES30.glUseProgram(program)
        texCoordHandle = GLES30.glGetAttribLocation(program, "a_texcoord")
        posCoordHandle = GLES30.glGetAttribLocation(program, "a_position")
        bindAttributes()
    }


    fun render(textBuffer: FloatBuffer, posBuffer: FloatBuffer) {
        if ((EGLContext.getEGL() as EGL10).eglGetCurrentContext() == EGL10.EGL_NO_CONTEXT) {
            throw IllegalStateException("Current thread has no openGL Context attached")
        }
        if (program == 0) init()

        GLES30.glUseProgram(program)
        GLES30.glVertexAttribPointer(
            texCoordHandle,
            2,
            GLES30.GL_FLOAT,
            false,
            0,
            textBuffer
        )
        GLES30.glEnableVertexAttribArray(texCoordHandle)

        GLES30.glVertexAttribPointer(
            posCoordHandle,
            2,
            GLES30.GL_FLOAT,
            false,
            0,
            posBuffer
        )
        GLES30.glEnableVertexAttribArray(posCoordHandle)

        setValues()
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(texCoordHandle)
        GLES20.glDisableVertexAttribArray(posCoordHandle)
    }

    protected open fun bindAttributes() {
        //no op
    }

    protected open fun setValues() {
        //no op
    }

    fun clear() {
        GLES30.glDeleteProgram(program)
        program = 0
    }
}
