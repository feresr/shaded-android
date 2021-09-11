#version 300 es
precision highp float;
uniform sampler2D tex_sampler;
uniform float grain;

in vec2 v_texcoord;
out vec4 FragColor;

float Grain(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}


void main()
{
    lowp vec4 textureColor = texture(tex_sampler, v_texcoord);

    vec3 grainPlate = vec3(Grain(v_texcoord));
    vec3 mixer = mix(textureColor.rgb, grainPlate, grain);
    FragColor = vec4(mixer, textureColor.w);
}
