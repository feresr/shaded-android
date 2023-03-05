package com.feresr.shaded

import android.content.Context
import android.opengl.GLES10.glGetError
import android.opengl.GLES20
import android.opengl.GLES30.GL_TRIANGLE_STRIP
import android.opengl.GLES30.glDrawArrays
import android.opengl.GLU
import androidx.annotation.RawRes
import com.feresr.shaded.opengl.Shader

open class Filter(
    val context: Context,
    @RawRes val fshader: Int = R.raw.fragment,
    @RawRes val vshader: Int = R.raw.vertex
) {

    private var initialized = false
    protected lateinit var shader: Shader

    fun render() {
        if (!initialized) {
            val fragment = context.resources.openRawResource(fshader).reader().use { it.readText() }
            val vertex = context.resources.openRawResource(vshader).reader().use { it.readText() }
            shader = Shader(fragment, vertex)
            bindUniforms()
            val glError = glGetError()
            if (glError != GLES20.GL_NO_ERROR) {
                throw RuntimeException("Failed to initialise filter: ${GLU.gluErrorString(glError)}");
            }
            initialized = true
        }
        shader.bind {
            //TODO: SEND THE IMAGE DIMENSIONS HERE? SOME SHADERS NEED IT!
            uploadUniforms()
            if (glGetError() != GLES20.GL_NO_ERROR) {
                throw RuntimeException("uniforms uploaded in correctly");
            }
            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
            val error = glGetError()
            if (error != GLES20.GL_NO_ERROR) {
                throw RuntimeException("filter applied in correctly + $error");
            }
        }
    }

    open fun updateUniforms(vararg value: Float) {}

    protected open fun bindUniforms() {
        //no op
    }

    protected open fun uploadUniforms() {
        //no op
    }


    /**
     * This resizes the dst before drawing the filter.
     * Useful for trading speed for quality. (blur filter is a good candidate)
     */
    open fun getDownscaleFactor(): Int = 1

    fun delete() {
        shader.delete()
        initialized = false
    }

}
