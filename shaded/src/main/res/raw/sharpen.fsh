#version 300 es
precision mediump float;

uniform highp float centerMultiplier;
uniform highp float edgeMultiplier;

uniform sampler2D tex_sampler;
in vec2 v_texcoord;

out vec4 FragColor;

void main()
{
    mediump vec3 textureColor = texture(tex_sampler, v_texcoord).rgb;
    mediump vec3 leftTextureColor = texture(tex_sampler, vec2(-1.0, .0)).rgb;
    mediump vec3 rightTextureColor = texture(tex_sampler, vec2(1.0, .0)).rgb;
    mediump vec3 topTextureColor = texture(tex_sampler, vec2(.0, 1.0)).rgb;
    mediump vec3 bottomTextureColor = texture(tex_sampler, vec2(.0, -.1)).rgb;

    FragColor = vec4((textureColor * centerMultiplier - (leftTextureColor * edgeMultiplier + rightTextureColor * edgeMultiplier + topTextureColor * edgeMultiplier + bottomTextureColor * edgeMultiplier)), texture(tex_sampler, bottomTextureCoordinate).w);
}
