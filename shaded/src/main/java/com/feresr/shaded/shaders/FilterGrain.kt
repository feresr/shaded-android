package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterGrain(context: Context) : Filter(context, R.raw.grain) {

    private var location = 0
    private var grain: Float = 0f

    fun updateUniforms(vararg value: Float) {
        grain = value[0]
    }

    override fun bindUniforms() {
        super.bindUniforms()
        location = shader.getUniformLocation("grain")
    }

    override fun uploadUniforms() {
        super.uploadUniforms()
        shader.setFloat(location, grain)
    }
}
