#version 300 es
precision mediump float;
uniform float saturation;
uniform sampler2D tex_sampler;
in vec2 v_texcoord;

out vec4 FragColor;

// Values from "Graphics Shaders: Theory and Practice" by Bailey and Cunningham
const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);

void main()
{
    lowp vec4 textureColor = texture(tex_sampler, v_texcoord);
    lowp float luminance = dot(textureColor.rgb, luminanceWeighting);
    lowp vec3 greyScaleColor = vec3(luminance);
    FragColor = vec4(mix(greyScaleColor, textureColor.rgb, saturation * 2.0), textureColor.w);
}
