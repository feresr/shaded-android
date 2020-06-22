package com.feresr.shaded.shaders

import android.content.Context
import android.opengl.GLES30
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterInverse(
    context: Context,
    @Volatile var alpha: Float
) : Filter(context, R.raw.inverse) {

    override fun bindUniforms() {
        super.bindUniforms()
        GLES30.glUniform1f(GLES30.glGetUniformLocation(program, "alpha"), alpha)
    }
}
