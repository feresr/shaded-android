#version 300 es
precision mediump float;
uniform sampler2D tex_sampler;
uniform float brightness;

in vec2 v_texcoord;
out vec4 FragColor;

void main()
{
    lowp vec4 textureColor = texture(tex_sampler, v_texcoord);
    float n = (-1.0 + 2.0 * brightness) * 0.20;
    FragColor = vec4((textureColor.rgb + vec3(n)), textureColor.w);
}
