#version 300 es
precision mediump float;
uniform float contrast;
uniform sampler2D tex_sampler;
in vec2 v_texcoord;

out vec4 FragColor;

float Normalize(float val, float valmin, float valmax, float min, float max, float midpoint) {
    float mid = (valmin + valmax) / 2.0;
    if (val < mid) {
        return (val - valmin) / (mid - valmin) * (midpoint - min) + min;
    }
    else {
        return (val - mid) / (valmax - mid) * (max - midpoint) + midpoint;
    }
}

void main()
{
    lowp vec4 textureColor = texture(tex_sampler, v_texcoord);
    float n = Normalize(contrast, 0.0, 1.0, .50, 1.50, 1.0);
    FragColor = vec4(mix(vec3(0.5), textureColor.xyz, n), textureColor.w);
}
