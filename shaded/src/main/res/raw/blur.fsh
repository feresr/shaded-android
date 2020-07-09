#version 300 es

in vec2 v_texcoord;

uniform sampler2D tex_sampler;
uniform vec2 blurRadius;

out vec4 FragColor;

float rand(in vec2 co) {
    float a = 12.9898;
    float b = 78.233;
    float c = 43758.5453;
    float dt = dot(co.xy, vec2(a, b));
    float sn = mod(dt, 3.14);
    return fract(sin(sn) * c);
}

vec4 blur(in vec2 radius) {
    vec4 color = vec4(0.0, 0.0, 0.0, 0.0);
    float total = 0.0;

    float offset = rand(gl_FragCoord.xy);
    float size = 10.0;
    float start = -size + offset - 0.5;
    float end = size + offset - 0.5;
    for (float t = start; t <= end; t++) {
        float percent = t / size;
        float weight = 1.0 - abs(percent);
        vec2 sampleVec = v_texcoord + radius * percent;
        if (sampleVec.x <= 1.0 && sampleVec.y <= 1.0) {
            vec4 sampleColor = texture(tex_sampler, sampleVec);
            color += sampleColor * weight;
            total += weight;
        }
    }

    color /= total;
    return color;
}

void main() {
    FragColor = blur(blurRadius);
}
