package com.feresr.shaded

import android.content.Context
import android.opengl.GLES30
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
            return throw IllegalStateException("Current thread has no openGL Context attached")
        }
        if (program == 0) {
            program = loadProgram(
                context.resources.openRawResource(shader).reader().readText(),
                context.resources.openRawResource(R.raw.vertex).reader().readText()
            )
            texCoordHandle = GLES30.glGetAttribLocation(program, "a_texcoord")
            posCoordHandle = GLES30.glGetAttribLocation(program, "a_position")
            bindAttributes()
        }
        GLES30.glUseProgram(program)
        //Puts texture coordinates data into `texCoordHandle (aka a_textcoord)` for this program
        GLES30.glVertexAttribPointer(
            texCoordHandle,
            2,
            GLES30.GL_FLOAT,
            false,
            0,
            textBuffer
        )
        GLES30.glEnableVertexAttribArray(texCoordHandle)

        //Puts texture coordinates data into `posCoordHandle (aka a_position)` for this program
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

        GLES30.glDisableVertexAttribArray(texCoordHandle)
        GLES30.glDisableVertexAttribArray(posCoordHandle)
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
