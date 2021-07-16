#version 300 es
precision highp float;
in vec2 v_texcoord;

uniform sampler2D tex_sampler;

uniform vec3 values[8];

out vec4 FragColor;

//RGB to HSL (hue, saturation, lightness/luminance).
//Source: https://gist.github.com/yiwenl/745bfea7f04c456e0101
vec3 rgb2hsl(vec3 c){
    float cMin=min(min(c.r, c.g), c.b),
    cMax=max(max(c.r, c.g), c.b),
    delta=cMax-cMin;
    vec3 hsl=vec3(0., 0., (cMax+cMin)/2.);
    if (delta!=0.0){ //If it has chroma and isn't gray.
        if (hsl.z<.5){
            hsl.y=delta/(cMax+cMin);//Saturation.
        } else {
            hsl.y=delta/(2.-cMax-cMin);//Saturation.
        }
        float deltaR=(((cMax-c.r)/6.)+(delta/2.))/delta,
        deltaG=(((cMax-c.g)/6.)+(delta/2.))/delta,
        deltaB=(((cMax-c.b)/6.)+(delta/2.))/delta;
        //Hue.
        if (c.r==cMax){
            hsl.x=deltaB-deltaG;
        } else if (c.g==cMax){
            hsl.x=(1./3.)+deltaR-deltaB;
        } else { //if(c.b==cMax){
            hsl.x=(2./3.)+deltaG-deltaR;
        }
        hsl.x=fract(hsl.x);
    }
    return hsl;
}


vec3 hue2rgb(float hue){
    hue = fract(hue);
    return clamp(vec3(abs(hue*6.-3.)-1., 2.-abs(hue*6.-2.), 2.-abs(hue*6.-4.)), 0., 1.);
}

vec3 hsl2rgb(vec3 hsl){
    if (hsl.y==0.){
        return vec3(hsl.z);//Luminance.
    } else {
        float b;
        if (hsl.z<.5){
            b=hsl.z*(1.+hsl.y);
        } else {
            b=hsl.z+hsl.y-hsl.y*hsl.z;
        }
        float a=2.*hsl.z-b;
        return a+hue2rgb(hsl.x)*(b-a);
    }
}

vec3 hsl_to_rgb(vec3 HSL)
{
    vec3 RGB = clamp(vec3(abs(HSL.x * 6.0 - 3.0) - 1.0, 2.0 - abs(HSL.x * 6.0 - 2.0), 2.0 - abs(HSL.x * 6.0 - 4.0)), vec3(0.0f), vec3(1.0f));
    float C = (1.0 - abs(2.0f * HSL.z - 1.0f)) * HSL.y;
    return (RGB - 0.5f) * C + HSL.z;
}

vec3 rgb_to_hcv(vec3 RGB)
{
    RGB = clamp(RGB, 0.0, 1.0);
    float Epsilon = 1e-10f;
    // Based on work by Sam Hocevar and Emil Persson
    vec4 P = (RGB.g < RGB.b) ? vec4(RGB.bg, -1.0f, 2.0/3.0) : vec4(RGB.gb, 0.0, -1.0/3.0);
    vec4 Q = (RGB.r < P.x) ? vec4(P.xyw, RGB.r) : vec4(RGB.r, P.yzx);
    float C = Q.x - min(Q.w, Q.y);
    float H = abs((Q.w - Q.y) / (6.0 * C + Epsilon) + Q.z);
    return vec3(H, C, Q.x);
}

vec3 rgb_to_hsl(vec3 RGB)
{
    vec3 HCV = rgb_to_hcv(RGB);
    float L = HCV.z - HCV.y * 0.5;
    float S = HCV.y / (1.0000001 - abs(L * 2.0 - 1.0));
    return vec3(HCV.x, S, L);
}

void main ()
{
    // Sample the input pixel
    vec4 color = texture(tex_sampler, v_texcoord);

    // Calcualte distance to primary colors
    float impacts[12];

    // convert the sampled color to HSL
    vec3 hsl = rgb_to_hsl(color.rgb);
    float max = 1.0f/12.0f;

    for (int i = 0; i < 12; i++) {
        float diff = hsl.x - (float(i) / 12.0f);
        float diffT = min(abs(diff), 1.0f - abs(diff));
        impacts[i] = clamp(diffT, 0.0, max);
        impacts[i] = impacts[i] / max;//normalise

        impacts[i] = 1.0 - impacts[i]; // invert
        // consenoudeal
        //impacts[i] = 0.5f * (cos(impacts[i] * 3.1415926f) + 1.0f);
    }

    vec3[12] rvalues = vec3[12](
    values[0], // red
    values[1], // orange
    values[2], // yellow
    values[3], //invisible
    values[3], //green
    values[3], // invisible
    values[4], // cyan
    (values[4] + values[5]) / 2.0f, // invisible
    values[5],
    values[6],
    values[7],
    (values[7] + values[0]) / 2.0f);


    // rvalues: -1 to 1 (how much to affect)
    // impact: should this go from 0 to 1 (how much this color/fragment should be affected)
    //    for (int i = 0; i < 12; i++) {
    //        hsl.x += rvalues[i][0] * impacts[i] * 0.25f;// hue goes from -1.0f/12.0f to 1.0f/12.0f
    //        hsl.y *= 1.0 + (rvalues[i][1] * impacts[i]);
    //        hsl.z *= 1.0 + rvalues[i][2] * impacts[i] * 6.0f;
    //    }




    vec3 tcolor = vec3(0.0f);
    for (int i= 0; i < 12; i++) {
        float huefactor = (max * float(i)) + (rvalues[i][0]/12.0f);
        if (huefactor < 0.0) huefactor = 1.0 + huefactor;
        if (huefactor > 1.0) huefactor = huefactor - 1.0;

        tcolor += impacts[i] * hsl_to_rgb(
        vec3(
        huefactor,
        clamp(hsl.y + hsl.y * rvalues[i][1], 0.0f, 1.0f),
        hsl.z * exp2(sqrt(hsl.y) * rvalues[i][2] * (1.0f - hsl.z) * hsl.y)
        )
        );

    }

    // convert back to rgb
    //color.xyz = hsl2rgb(hsl);

    // Save the result
    FragColor = vec4(tcolor, 1.0f);
}




