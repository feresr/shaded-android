package com.feresr.shaded

import android.content.Context
import android.opengl.GLES30.GL_TRIANGLE_STRIP
import android.opengl.GLES30.glDeleteProgram
import android.opengl.GLES30.glDrawArrays
import android.opengl.GLES30.glUseProgram
import androidx.annotation.RawRes
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLContext


abstract class Filter(val context: Context, @RawRes val shader: Int) {

    protected var program: Int = 0

    fun init() {
        program = loadProgram(
            context.resources.openRawResource(shader).reader().readText(),
            context.resources.openRawResource(R.raw.vertex).reader().readText()
        )
        bindUniforms()
    }

    /**
     * Renders the image to the currently bounded FBO
     */
    fun render() {
        if ((EGLContext.getEGL() as EGL10).eglGetCurrentContext() == EGL10.EGL_NO_CONTEXT) {
            throw IllegalStateException("Current thread has no openGL Context attached")
        }
        glUseProgram(program)
        updateUniforms()
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
        glUseProgram(0)
    }

    protected open fun bindUniforms() {
        //no op
    }

    protected open fun updateUniforms() {
        //no op
    }

    fun delete() {
        glDeleteProgram(program)
    }
}
