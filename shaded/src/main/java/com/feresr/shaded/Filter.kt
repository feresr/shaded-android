package com.feresr.shaded

import android.content.Context
import android.opengl.GLES30.GL_TRIANGLE_STRIP
import android.opengl.GLES30.glDrawArrays
import androidx.annotation.RawRes
import com.feresr.shaded.opengl.Shader

abstract class Filter(val context: Context, @RawRes val fshader: Int) {

    protected val shader: Shader by lazy {
        Shader(
            "",
            context.resources.openRawResource(fshader).reader().readText(),
            context.resources.openRawResource(R.raw.vertex).reader().readText()
        );
    }

    fun init() = bindUniforms()

    fun render() {
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
    }
}
