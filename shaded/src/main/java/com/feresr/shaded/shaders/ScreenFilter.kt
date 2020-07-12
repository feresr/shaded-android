package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

/**
 * For internal use, renders the final image to the default frame buffer object.
 */
internal class ScreenFilter(context: Context) : Filter(context, vshader = R.raw.vertexscreen) {

    var model: FloatArray = FloatArray(16)
    var camera: FloatArray = FloatArray(16)
    var projection: FloatArray = FloatArray(16)

    private var modelLocation = 0
    private var camLocation = 0
    private var projLocation = 0

    override fun bindUniforms() {
        super.bindUniforms()
        modelLocation = shader.getUniformLocation("model")
        camLocation = shader.getUniformLocation("camera")
        projLocation = shader.getUniformLocation("projection")
    }

    override fun updateUniforms() {
        super.updateUniforms()
        shader.setMat4(modelLocation, model )
        shader.setMat4(camLocation, camera)
        shader.setMat4(projLocation, projection, true)
    }

}
