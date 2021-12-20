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
            val fragment = context.resources.openRawResource(fshader).reader().use { it.readText() }
            val vertex = context.resources.openRawResource(vshader).reader().use { it.readText() }
            shader = Shader(fragment, vertex)
            bindUniforms()
            initialized = true
        }
        shader.bind {
            //TODO: SEND THE IMAGE DIMENSIONS HERE? SOME SHADERS NEED IT!
            uploadUniforms()
            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
        }
    }

    open fun updateUniforms(vararg value: Float) {}

    protected open fun bindUniforms() {
        //no op
    }

    protected open fun uploadUniforms() {
        //no op
    }

    fun delete() {
        shader.delete()
        initialized = false
    }

}
