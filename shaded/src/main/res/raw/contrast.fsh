#version 300 es
precision mediump float;
uniform float contrast;
uniform sampler2D tex_sampler;
in vec2 v_texcoord;

out vec4 FragColor;

void main()
{
    lowp vec4 textureColor = texture(tex_sampler, v_texcoord);
    float n = 0.25 + contrast * 1.5;
    FragColor = vec4(mix(vec3(0.5), textureColor.xyz, n), textureColor.w);
}
