#version 300 es
precision mediump float;
uniform sampler2D tex_sampler;
uniform float vibrance;

in vec2 v_texcoord;
out vec4 FragColor;

void main()
{
    lowp vec4 color = texture(tex_sampler, v_texcoord);
    lowp float average = (color.r + color.g + color.b) / 3.0;
    lowp float mx = max(color.r, max(color.g, color.b));
    lowp float amt = (mx - average) * (-(-1.5 + 3.0 * vibrance) * 3.0);
    color.rgb = mix(color.rgb, vec3(mx), amt);
    FragColor = color;
}
