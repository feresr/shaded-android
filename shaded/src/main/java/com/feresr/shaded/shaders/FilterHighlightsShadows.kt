package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterHighlightsShadows(context: Context) : Filter(context, R.raw.highlights_shadows) {

    private var highlights: Float = 1.0f
    private var shadows: Float = 0.0f

    private var hLocation = 0
    private var sLocation = 0

    override fun updateUniforms(value: FloatArray) {
        highlights = value[0]
        shadows = value[1]
    }

    override fun bindUniforms() {
        super.bindUniforms()
        hLocation = shader.getUniformLocation("highlights")
        sLocation = shader.getUniformLocation("shadows")
    }

    override fun uploadUniforms() {
        super.uploadUniforms()
        shader.setFloat(hLocation, highlights)
        shader.setFloat(sLocation, shadows)
    }
}
