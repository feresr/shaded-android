package com.feresr.shaded.shaders

import android.content.Context
import android.opengl.GLES30
import com.feresr.shaded.Filter
import com.feresr.shaded.R
import java.nio.FloatBuffer

class FilterBlur(
    context: Context,
    @Volatile var x: Float = 0f,
    @Volatile var y: Float = 0f
) : Filter(context, R.raw.blur) {

    private var blurRadius = 0

    override fun bindUniforms() {
        super.bindUniforms()
        blurRadius = shader.getUniformLocation("blurRadius")
    }

    override fun updateUniforms() {
        super.updateUniforms()
        GLES30.glUniform2fv(blurRadius, 1, FloatBuffer.wrap(floatArrayOf(x, y)))
    }
}
