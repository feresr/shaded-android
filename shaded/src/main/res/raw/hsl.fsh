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

void main ()
{
    // Sample the input pixel
    vec4 color = texture(tex_sampler, v_texcoord);

    // Calcualte distance to primary colors
    float impacts[12];

    // convert the sampled color to HSL
    vec3 hsl = rgb2hsl(color.rgb);

    for (int i = 0; i < 12; i++) {
        float diff = hsl.x - (float(i) / 12.0f);
        float diffT = min(diff, 1.0 - diff);
        float max = 1.0f/12.0f;
        impacts[i] = clamp(max - abs(diffT), 0.0, max);
    }

    vec3[12] rvalues = vec3[12](
        values[0],
        values[1],
        values[2],
        (values[2] + values[3]) / 2.0f,
        values[3],
        (values[3] + values[4]) / 2.0f,
        values[4],
        (values[4] + values[5]) / 2.0f,
        values[5],
        values[6],
        values[7],
        (values[7] + values[0]) / 2.0f
    );

    for (int i = 0; i < 12; i++) {
        hsl.x += rvalues[i][0] * impacts[i];
        hsl.y += rvalues[i][1] * impacts[i];
        hsl.z += rvalues[i][2] * impacts[i];
    }

    // convert back to rgb
    color.xyz = hsl2rgb(hsl);

    // Save the result
    FragColor = color;
}




