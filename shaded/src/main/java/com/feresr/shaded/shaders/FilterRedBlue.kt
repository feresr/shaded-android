package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterRedBlue(context: Context) : Filter(context, fshader = R.raw.redblue) {

    private var adjust: Float = 0.0f
    private var location = 0
    fun updateUniforms(value: FloatArray) {
        adjust = value[0]
    }

    override fun bindUniforms() {
        super.bindUniforms()
        //location = shader.getUniformLocation("adjust")
    }

    override fun uploadUniforms() {
        super.uploadUniforms()
        //shader.setFloat(location, 1.0f - adjust)
    }
}
