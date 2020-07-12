#version 300 es
precision mediump float;
layout(location = 0) in vec2 a_position;
layout(location = 1) in vec2 a_texcoord;

uniform mat4 model;
uniform mat4 camera;
uniform mat4 projection;

out vec2 v_texcoord;

void main() {
    gl_Position = projection * camera * model * vec4(a_position, 0.0, 1.0);
    v_texcoord = vec2(a_texcoord.x, 1.0) - vec2(0.0, a_texcoord.y);
}
