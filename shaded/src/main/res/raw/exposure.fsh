#version 300 es
precision mediump float;
uniform sampler2D tex_sampler;
uniform float exposure;

in vec2 v_texcoord;
out vec4 FragColor;

void main()
{
    vec3 hdrColor = texture(tex_sampler, v_texcoord).rgb;
    hdrColor = hdrColor * exp2(exposure);
    FragColor = vec4(hdrColor, 1.0);
}
