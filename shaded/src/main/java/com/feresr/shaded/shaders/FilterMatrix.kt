package com.feresr.shaded.shaders

import android.content.Context
import android.graphics.Matrix
import androidx.core.graphics.values
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterMatrix(context: Context) : Filter(context, vshader = R.raw.matrix) {

    var model: FloatArray = Matrix().values()
    var camera: FloatArray = Matrix().values()
    var projection: FloatArray = Matrix().values()

    private var modelLocation = 0
    private var camLocation = 0
    private var projLocation = 0

    fun setModelMatrix(matrix: Matrix) {
        this.model = matrix.values()
    }

    override fun bindUniforms() {
        super.bindUniforms()
        modelLocation = shader.getUniformLocation("model")
        camLocation = shader.getUniformLocation("camera")
        projLocation = shader.getUniformLocation("projection")
    }

    override fun uploadUniforms() {
        super.uploadUniforms()
        shader.setMat3(modelLocation, model)
        shader.setMat3(camLocation, camera)
        shader.setMat3(projLocation, projection)
    }

}
