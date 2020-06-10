#version 320 es
#ifdef GL_ES
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif
#endif
uniform sampler2D tex_sampler;
in vec2 v_texcoord;
out vec4 fragColor;
void main() {
    fragColor = texture(tex_sampler, v_texcoord);
}
