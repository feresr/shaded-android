#version 300 es

uniform float contrast;
uniform sampler2D tex_sampler;
in vec2 v_texcoord;

out vec4 FragColor;

void main()
{
    lowp vec4 textureColor = texture(tex_sampler, v_texcoord);
    FragColor = vec4(((textureColor.rgb - vec3(0.5)) * contrast + vec3(0.5)), textureColor.w);
}
