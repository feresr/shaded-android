#version 320 es
#ifdef GL_ES
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif
#endif
out vec4 fragColor;
in vec2 texCoords;
uniform sampler2D tex_sampler;
void main() {
    vec4 texColor = texture(tex_sampler, texCoords);
    //if (texColor.a < 0.1)
    //    discard;
    fragColor = texColor;
}


