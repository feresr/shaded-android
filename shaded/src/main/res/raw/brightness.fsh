#version 100
#ifdef GL_ES
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif
#endif


uniform sampler2D tex_sampler;
varying vec2 v_texcoord;
uniform lowp float brightness;

void main()
{
    lowp vec4 textureColor = texture2D(tex_sampler, v_texcoord);
    gl_FragColor = vec4((textureColor.rgb + vec3(brightness)), textureColor.w);
}
