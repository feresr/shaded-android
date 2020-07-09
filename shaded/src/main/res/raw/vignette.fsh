#version 300 es

precision mediump float;
uniform sampler2D tex_sampler;
uniform vec2 vignetteCenter;
uniform vec3 vignetteColor;
uniform float vignetteStart;
uniform float vignetteEnd;

in vec2 v_texcoord;
out vec4 FragColor;

void main()
{
    vec3 rgb = texture(tex_sampler, v_texcoord).rgb;
    float d = distance(v_texcoord, vec2(vignetteCenter.x, vignetteCenter.y));
    float percent = smoothstep(vignetteStart, vignetteEnd, d);
    FragColor = vec4(mix(rgb.x, vignetteColor.x, percent), mix(rgb.y, vignetteColor.y, percent), mix(rgb.z, vignetteColor.z, percent), 1.0);
}
