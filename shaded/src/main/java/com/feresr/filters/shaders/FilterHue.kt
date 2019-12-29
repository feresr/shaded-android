package com.feresr.filters.shaders

import android.content.Context
import android.opengl.GLES30
import com.feresr.filters.Filter
import com.feresr.filters.R

class FilterHue(context: Context, val value: () -> Float) : Filter(context,
    R.raw.hue
) {
    private var hue = 0
    override fun bindAttributes() {
        super.bindAttributes()
        hue = GLES30.glGetUniformLocation(program, "hueAdjust")

    }

    override fun setValues() {
        super.setValues()
        GLES30.glUniform1f(hue, value())
    }
}
