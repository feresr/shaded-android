#version 100
#ifdef GL_ES
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif
#endif

uniform float contrast;
uniform sampler2D tex_sampler;
varying vec2 v_texcoord;

void main()
{
    lowp vec4 textureColor = texture2D(tex_sampler, v_texcoord);
    gl_FragColor = vec4(((textureColor.rgb - vec3(0.5)) * contrast + vec3(0.5)), textureColor.w);
}
