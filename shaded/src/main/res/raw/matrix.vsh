#version 300 es
precision mediump float;
layout(location = 0) in vec2 a_position;
layout(location = 1) in vec2 a_texcoord;

uniform mat3 model;
uniform mat3 camera;
uniform mat3 projection;

out vec2 v_texcoord;

void main() {
    vec3 projected = vec3(a_position, 1.0f) * model * camera * projection;
    gl_Position = vec4(projected, 1.0);
    v_texcoord = vec2(a_texcoord.x, 1.0) - vec2(0.0, a_texcoord.y);
}
