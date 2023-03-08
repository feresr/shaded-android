package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterSharpen(context: Context) : Filter(context, R.raw.sharpen) {

    private var location = 0
    private var sharpen: Float = 1.0f

    fun updateUniforms(value: FloatArray) {
        sharpen = value[0]
    }

    override fun bindUniforms() {
        super.bindUniforms()
        location = shader.getUniformLocation("value")
    }

    override fun uploadUniforms() {
        super.uploadUniforms()
        shader.setFloat(location, sharpen)
    }
}
