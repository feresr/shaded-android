package com.feresr.shaded.shaders

enum class UniformType {
    INT,
    FLOAT
}

data class Uniform(
    val name: String,
    val type: UniformType,
    val location: Int
)
