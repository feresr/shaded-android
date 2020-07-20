package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

/**
The degree of saturation or desaturation to apply to the image (0.0 - 2.0, with 1.0 as the default)
 **/
class FilterSaturation(context: Context, @Volatile var saturation: Float = 1.0f) :
    Filter(context, R.raw.saturation) {

    private var value = 0

    override fun bindUniforms() {
        super.bindUniforms()
        value = shader.getUniformLocation("saturation")
    }

    override fun updateUniforms() {
        super.updateUniforms()
        shader.setFloat(value, saturation)
    }
}
