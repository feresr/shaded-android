package com.feresr.shaded.shaders

import android.content.Context
import android.opengl.GLES30
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterHue(
    context: Context,
    @Volatile var value: Float
) : Filter(context, R.raw.hue) {
    private var hue = 0
    override fun bindAttributes() {
        super.bindAttributes()
        hue = GLES30.glGetUniformLocation(program, "hueAdjust")

    }

    override fun setValues() {
        super.setValues()
        GLES30.glUniform1f(hue, value)
    }
}
