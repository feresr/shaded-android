#version 300 es
precision mediump float;
layout(location = 0) in vec2 a_position;
layout(location = 1) in vec2 a_texcoord;

out vec2 v_texcoord;

uniform float frameAdjust;

void main() {
    gl_Position = vec4(a_position * frameAdjust, 0.0, 1.0);
    v_texcoord = a_texcoord;
}
