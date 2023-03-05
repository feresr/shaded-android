package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

enum class Direction {
    HORIZONTAL,
    VERTICAL
}

enum class Quality {
    REGULAR,
    HIGH,
}

class FilterBlur(
    context: Context,
    private val direction: Direction,
) :
    Filter(context, R.raw.blur) {

    private var radiusLocation = 0
    private var directionLocation = 0
    private var resolutionLocation = 0

    var radius = 0f
    var resolution = 0f to 0f
    var factor: Int = 1

    // params(radius : Float, resolutionX: Float, resolutionY: Float)
    override fun updateUniforms(vararg value: Float) {
    }

    override fun getDownscaleFactor(): Int {
        return factor
    }

    override fun bindUniforms() {
        super.bindUniforms()
        radiusLocation = shader.getUniformLocation("radius")
        directionLocation = shader.getUniformLocation("direction")
        resolutionLocation = shader.getUniformLocation("resolution")
    }

    override fun uploadUniforms() {
        shader.setFloat(radiusLocation, radius)
        shader.setFloat2(resolutionLocation, resolution.first, resolution.second)
        when (direction) {
            Direction.HORIZONTAL -> shader.setFloat2(directionLocation, 1.0f, 0.0f)
            Direction.VERTICAL -> shader.setFloat2(directionLocation, 0.0f, 1.0f)
        }
    }
}
