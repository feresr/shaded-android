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
uniform float alpha;

void main() {
    vec4 colors = texture2D(tex_sampler, v_texcoord);
    gl_FragColor = vec4(alpha - colors.x, alpha - colors.y, alpha - colors.z, 1.0);
}
