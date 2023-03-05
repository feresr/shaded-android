#version 300 es
precision mediump float;
uniform sampler2D tex_sampler;
in vec2 v_texcoord;

out vec4 FragColor;
void main() {
    FragColor = texture(tex_sampler, v_texcoord);
//    FragColor = vec4(v_texcoord.x, v_texcoord.y, 0.0, 1.0f);
}
