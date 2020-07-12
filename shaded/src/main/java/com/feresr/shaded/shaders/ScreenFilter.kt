package com.feresr.shaded.shaders

import android.content.Context
import android.renderscript.Matrix4f
import com.feresr.shaded.Filter
import com.feresr.shaded.R

/**
 * For internal use, renders the final image to the default frame buffer object.
 */
internal class ScreenFilter(context: Context) : Filter(context, vshader = R.raw.vertexscreen) {

    var model: Matrix4f = Matrix4f()
    var camera: Matrix4f = Matrix4f()
    var projection: Matrix4f = Matrix4f()

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
        shader.setMat4(modelLocation, model)
        shader.setMat4(camLocation, camera)
        shader.setMat4(projLocation, projection)
    }

}
