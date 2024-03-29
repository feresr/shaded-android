#version 300 es
precision mediump float;

uniform float highlights;
uniform float shadows;
uniform sampler2D tex_sampler;

const vec3 luminanceWeighting = vec3(0.33, 0.33, 0.33);

in vec2 v_texcoord;

out vec4 FragColor;

void main()
{
    vec4 source = texture(tex_sampler, v_texcoord);
    mediump float luminance = dot(source.rgb, luminanceWeighting);

    mediump float shadow = clamp((pow(luminance, 1.0/(shadows+1.0)) + (-0.76)*pow(luminance, 2.0/(shadows+1.0))) - luminance, 0.0, 1.0);
    mediump float highlight = clamp((1.0 - (pow(1.0-luminance, 1.0/(2.0-highlights)) + (-0.8)*pow(1.0-luminance, 2.0/(2.0-highlights)))) - luminance, -1.0, 0.0);
    lowp vec3 result = vec3(0.0, 0.0, 0.0) + ((luminance + shadow + highlight) - 0.0) * ((source.rgb - vec3(0.0, 0.0, 0.0))/(luminance - 0.0));
    FragColor = vec4(result.rgb, source.a);
}