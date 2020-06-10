#version 320 es

#ifdef GL_ES
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif
#endif
layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

out vec2 texCoords;

void build_house(vec4 position)
{
    gl_Position = position + vec4(-0.1, -0.1, 0.0, 0.0);    // 1:bottom-left
    texCoords = vec2(0.0, 0.0);
    EmitVertex();
    gl_Position = position + vec4( 0.1, -0.1, 0.0, 0.0);    // 2:bottom-right
    texCoords = vec2(1.0, 0.0);
    EmitVertex();
    gl_Position = position + vec4(-0.1,  0.1, 0.0, 0.0);    // 3:top-left
    texCoords = vec2(0.0, 1.0);
    EmitVertex();
    gl_Position = position + vec4( 0.1,  0.1, 0.0, 0.0);    // 4:top-right
    texCoords = vec2(1.0, 1.0);
    EmitVertex();
    EndPrimitive();
}

void main() {
    build_house(gl_in[0].gl_Position);
}
