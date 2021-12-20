package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterBlur(context: Context) : Filter(context, R.raw.blur) {

    private var location = 0
    private var x: Float = 0f
    private var y: Float = 0f

    override fun updateUniforms(vararg value: Float) {
        this.x = value[0]
        this.y = value[1]
    }

    override fun bindUniforms() {
        super.bindUniforms()
        location = shader.getUniformLocation("blurRadius")
    }

    override fun uploadUniforms() {
        super.uploadUniforms()
        shader.setFloat2(location, x, y)
    }
}
