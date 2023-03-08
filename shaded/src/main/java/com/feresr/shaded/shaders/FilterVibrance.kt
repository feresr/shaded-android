package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterVibrance(context: Context) : Filter(context, R.raw.vibrance) {

    private var location = 0
    private var vibrance: Float = 0.5f

    fun updateUniforms(value: FloatArray) {
        vibrance = value[0]
    }

    override fun bindUniforms() {
        super.bindUniforms()
        location = shader.getUniformLocation("vibrance")
    }

    override fun uploadUniforms() {
        super.uploadUniforms()
        shader.setFloat(location, vibrance)
    }
}
