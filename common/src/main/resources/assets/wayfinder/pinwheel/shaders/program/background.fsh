#version 150

uniform sampler2D Sampler0;
uniform sampler2D Palette;
uniform sampler2D Noise;

in vec2 texCoord0;
in vec2 noiseCoord;

out vec4 fragColor;

const vec3 light = vec3(252., 249., 235.) / 255.;
const vec3 dark = vec3(240., 231., 213.) / 255.;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if(color.a == 0) {
        discard;
    }
    if (color == vec4(1.,0.,1.,1.)) {
        float noise = 1. - texture(Noise, noiseCoord * 0.001).r;
        color = vec4(mix(light, dark, noise), 1.);
    }

    fragColor = vec4(color.rgb, 1.0);
}