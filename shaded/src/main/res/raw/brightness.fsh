#version 300 es

uniform sampler2D tex_sampler;
uniform float brightness;

in vec2 v_texcoord;
out vec4 FragColor;

void main()
{
    lowp vec4 textureColor = texture(tex_sampler, v_texcoord);
    FragColor = vec4((textureColor.rgb + vec3(brightness)), textureColor.w);
}
