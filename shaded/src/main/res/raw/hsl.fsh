#version 300 es
precision highp float;
in vec2 v_texcoord;

uniform sampler2D tex_sampler;

uniform vec3 values[8];

out vec4 FragColor;

float originalHue[9] = float[9](
    0.0,                    // red
    0.0833333333333,        // orange
    0.1666666666666,        // yellow
    0.3333333333333,        // green
    0.5,                    // cyan
    0.6666666666666,        // blue
    0.75,                   // purple
    0.8333333333333,        // magenta
    1.0                     // red
);

vec3 hsl_to_rgb(vec3 HSL) {
    vec3 RGB = clamp(vec3(abs(HSL.x * 6.0 - 3.0) - 1.0, 2.0 - abs(HSL.x * 6.0 - 2.0), 2.0 - abs(HSL.x * 6.0 - 4.0)), vec3(0.0f), vec3(1.0f));
    float C = (1.0 - abs(2.0f * HSL.z - 1.0f)) * HSL.y;
    return (RGB - 0.5f) * C + HSL.z;
}

vec3 rgb_to_hcv(vec3 RGB) {
    RGB = clamp(RGB, 0.0, 1.0);
    float Epsilon = 1e-10f;
    // Based on work by Sam Hocevar and Emil Persson
    vec4 P = (RGB.g < RGB.b) ? vec4(RGB.bg, -1.0f, 2.0/3.0) : vec4(RGB.gb, 0.0, -1.0/3.0);
    vec4 Q = (RGB.r < P.x) ? vec4(P.xyw, RGB.r) : vec4(RGB.r, P.yzx);
    float C = Q.x - min(Q.w, Q.y);
    float H = abs((Q.w - Q.y) / (6.0 * C + Epsilon) + Q.z);
    return vec3(H, C, Q.x);
}

vec3 rgb_to_hsl(vec3 RGB) {
    vec3 HCV = rgb_to_hcv(RGB);
    float L = HCV.z - HCV.y * 0.5;
    float S = HCV.y / (1.0000001 - abs(L * 2.0 - 1.0));
    return vec3(HCV.x, S, L);
}

float saturate(float v) {
    return clamp(v, 0.0f, 1.0f);
}

void main ()
{
    // Sample the input pixel
    vec4 color = texture(tex_sampler, v_texcoord);

    // convert the sampled color to HSL
    vec3 hsl = rgb_to_hsl(color.rgb);
    float maxL = 1.0f/12.0f;

    float red = max(
    saturate(1.0 - abs((hsl.x -  0.0/12.0) * 12.0)),
    saturate(1.0 - abs((hsl.x - 12.0/12.0) * 6.0))
    );

    float yellow = max(
    saturate(1.0 - abs((hsl.x -  2.0/12.0) * 12.0)) * step(hsl.x, 2.0/12.0),
    saturate(1.0 - abs((hsl.x -  2.0/12.0) * 6.0)) * step(2.0/12.0, hsl.x)
    );

    float magenta = max(
    saturate(1.0 - abs((hsl.x - 10.0/12.0) * 12.0)) * step(hsl.x, 10.0/12.0),
    saturate(1.0 - abs((hsl.x - 10.0/12.0) * 6.0)) * step(10.0/12.0, hsl.x)
    );

    float blue =  max(
    saturate(1.0 - abs((hsl.x -  8.0/12.0) * 12.0)) * step(8.0/12.0, hsl.x), //red left side - need this due to 360 degrees -> 0 degrees
    saturate(1.0 - abs((hsl.x - 8.0/12.0) * 6.0)) * step(hsl.x, 8.0/12.0)
    );

    float impacts[8] = float[8](
        red,
        saturate(1.0 - abs((hsl.x -  1.0/12.0) * 12.0)),    // orange both sides
        yellow,
        saturate(1.0 - abs((hsl.x -  4.0/12.0) * 6.0)),     // green both sides
        saturate(1.0 - abs((hsl.x -  6.0/12.0) * 6.0)),     // aqua both sides
        blue,
        saturate(1.0 - abs((hsl.x - 9.0/12.0) * 12.0)),     // purple both sides
        magenta                                             // magenta both sides
    );

    float huefactors[8];
    huefactors[0] = (values[0].x > 0.0) ? mix(originalHue[0], originalHue[1], values[0].x) : mix(originalHue[8], originalHue[7], -values[0].x);
    huefactors[1] = (values[1].x > 0.0) ? mix(originalHue[1], originalHue[2], values[1].x) : mix(originalHue[1], originalHue[0], -values[1].x);
    huefactors[2] = (values[2].x > 0.0) ? mix(originalHue[2], originalHue[3], values[2].x) : mix(originalHue[2], originalHue[1], -values[2].x);
    huefactors[3] = (values[3].x > 0.0) ? mix(originalHue[3], originalHue[4], values[3].x) : mix(originalHue[3], originalHue[2], -values[3].x);
    huefactors[4] = (values[4].x > 0.0) ? mix(originalHue[4], originalHue[5], values[4].x) : mix(originalHue[4], originalHue[3], -values[4].x);
    huefactors[5] = (values[5].x > 0.0) ? mix(originalHue[5], originalHue[6], values[5].x) : mix(originalHue[5], originalHue[4], -values[5].x);
    huefactors[6] = (values[6].x > 0.0) ? mix(originalHue[6], originalHue[7], values[6].x) : mix(originalHue[6], originalHue[5], -values[6].x);
    huefactors[7] = (values[7].x > 0.0) ? mix(originalHue[7], originalHue[8], values[7].x) : mix(originalHue[7], originalHue[6], -values[7].x);

    vec3 tcolor = vec3(0.0f);
    for (int i= 0; i < 8; i++) {
        tcolor += impacts[i] * hsl_to_rgb(
            vec3(
                huefactors[i],
                clamp(hsl.y + hsl.y * values[i][1], 0.0f, 1.0f),
                hsl.z * exp2(sqrt(hsl.y) * values[i][2] * (1.0f - hsl.z) * hsl.y)
            )
        );
    }

    // Save the result
    FragColor = vec4(tcolor, 1.0f);
}




