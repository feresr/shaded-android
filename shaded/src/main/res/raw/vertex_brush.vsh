#version 320 es

in vec4 a_position;
out vec2 v_texcoord;

void main() {
    gl_Position = a_position;
}
