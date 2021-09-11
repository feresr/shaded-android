#version 300 es
precision mediump float;

uniform highp float value;

uniform sampler2D tex_sampler;
in vec2 v_texcoord;

out vec4 FragColor;

vec3 texSample(const int x, const int y, in vec2 fragCoord)
{
    ivec2 size = textureSize(tex_sampler,0);
    return texture(tex_sampler, fragCoord + vec2(float(x)/float(size.x), float(y)/float(size.y))).xyz;
}
vec3 sharpenFilter(in vec2 fragCoord, float strength){
    vec3 f =
    texSample(-1,-1, fragCoord) *  -1. +
    texSample( 0,-1, fragCoord) *  -1. +
    texSample( 1,-1, fragCoord) *  -1. +
    texSample(-1, 0, fragCoord) *  -1. +
    texSample( 0, 0, fragCoord) *   9. +
    texSample( 1, 0, fragCoord) *  -1. +
    texSample(-1, 1, fragCoord) *  -1. +
    texSample( 0, 1, fragCoord) *  -1. +
    texSample( 1, 1, fragCoord) *  -1.
    ;
    return mix(texSample( 0, 0, fragCoord), f , strength);
}
void main(){
    FragColor = vec4(sharpenFilter(v_texcoord, value),1.0);
}