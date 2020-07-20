package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterHighlightsShadows(
    context: Context,
    @Volatile var highlights: Float = 1.0f,
    @Volatile var shadows: Float = 0.0f
) : Filter(context, R.raw.highlights_shadows) {

    private var hLocation = 0
    private var sLocation = 0

    override fun bindUniforms() {
        super.bindUniforms()
        hLocation = shader.getUniformLocation("highlights")
        sLocation = shader.getUniformLocation("shadows")
    }

    override fun updateUniforms() {
        super.updateUniforms()
        shader.setFloat(hLocation, highlights)
        shader.setFloat(sLocation, shadows)
    }
}
