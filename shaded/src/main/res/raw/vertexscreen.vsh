attribute vec4 a_position;
attribute vec2 a_texcoord;
varying vec2 v_texcoord;

void main() {
    gl_Position = a_position;
    v_texcoord = vec2(a_texcoord.x, 1.0) - vec2(0.0, a_texcoord.y);
}
