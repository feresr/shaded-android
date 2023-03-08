package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

/**
The degree of saturation or desaturation to apply to the image (0.0 - 2.0, with 1.0 as the default)
 **/
class FilterSaturation(context: Context) : Filter(context, R.raw.saturation) {
    private var saturation: Float = 1.0f
    private var location = 0

    fun updateUniforms(value: FloatArray) {
        saturation = value[0]
    }

    override fun bindUniforms() {
        super.bindUniforms()
        location = shader.getUniformLocation("saturation")
    }

    override fun uploadUniforms() {
        super.uploadUniforms()
        shader.setFloat(location, saturation)
    }
}
