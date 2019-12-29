package com.feresr.shaded.shaders

import android.content.Context
import android.opengl.GLES20
import com.feresr.shaded.Filter
import com.feresr.shaded.R
import java.nio.FloatBuffer

class FilterBlur(context: Context, val values: () -> Pair<Float, Float>) :
    Filter(context, R.raw.blur) {

    private var blurRadius = 0

    override fun bindAttributes() {
        super.bindAttributes()
        blurRadius = GLES20.glGetUniformLocation(program, "blurRadius")
    }

    override fun setValues() {
        super.setValues()
        val (x, y) = values()
        GLES20.glUniform2fv(blurRadius, 1, FloatBuffer.wrap(floatArrayOf(x, y)))
    }
}
