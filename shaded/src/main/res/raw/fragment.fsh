#version 100
#ifdef GL_ES
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif
#endif
uniform sampler2D tex_sampler;
varying vec2 v_texcoord;
void main() {
    gl_FragColor = texture2D(tex_sampler, v_texcoord);
}
