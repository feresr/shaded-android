package com.feresr.shaded

import android.content.Context
import android.opengl.GLES30.GL_TRIANGLE_STRIP
import android.opengl.GLES30.glDrawArrays
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
            shader = Shader(
                context.resources.openRawResource(fshader).reader().readText(),
                context.resources.openRawResource(vshader).reader().readText()
            )
            bindUniforms()
            initialized = true
        }
        shader.bind {
            updateUniforms()
            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
        }
    }

    protected open fun bindUniforms() {
        //no op
    }

    protected open fun updateUniforms() {
        //no op
    }

    fun delete() {
        shader.delete()
        initialized = false
    }
}
