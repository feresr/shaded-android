#version 300 es
precision mediump float;
uniform sampler2D tex_sampler;
uniform float exposure;

in vec2 v_texcoord;
out vec4 FragColor;

void main()
{
    lowp vec4 textureColor = texture(tex_sampler, v_texcoord);
    float n = -1.0 + (exposure * 2.0);
    vec3 exposureRGB = textureColor.rgb * pow(2.0, n);
    FragColor = vec4(exposureRGB, textureColor.w);
}
