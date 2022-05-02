#version 300 es
precision mediump float;
uniform sampler2D tex_sampler;

in vec2 v_texcoord;

out vec4 FragColor;

void main() {
    vec4 red = texture(tex_sampler, v_texcoord + vec2(0.01, 0.0)) * vec4(1.0, 0.5, 0.0, 1.0);
    vec4 blue = texture(tex_sampler, v_texcoord + vec2(-0.01, 0.0)) * vec4(0.0, 0.5, 1.0, 1.0);

    FragColor = mix(red, blue, .5f);
}
