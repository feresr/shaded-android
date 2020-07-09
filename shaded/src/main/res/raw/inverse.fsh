#version 300 es
precision mediump float;
uniform sampler2D tex_sampler;
uniform float alpha;

in vec2 v_texcoord;
out vec4 FragColor;

void main() {
    vec4 colors = texture(tex_sampler, v_texcoord);
    FragColor = vec4(alpha - colors.x, alpha - colors.y, alpha - colors.z, 1.0);
}
