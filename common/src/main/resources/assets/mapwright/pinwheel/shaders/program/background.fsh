#version 150

uniform sampler2D Sampler0;
uniform sampler2D Palette;
uniform sampler2D Noise;

in vec2 texCoord0;
in vec2 noiseCoord;

out vec4 fragColor;

const vec3 light = vec3(255., 252., 245.) / 255.;
const vec3 dark = vec3(245., 232., 198.) / 255.;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if(color.a == 0) {
        discard;
    }
    if (color == vec4(1.,0.,1.,1.)) {
        float noise = 1. - texture(Noise, noiseCoord * 0.001).r;
        color = vec4(mix(light, dark, noise), 1.);
    }
    // translucency signifies mixing with the noise texture
//    if (color.a < 1) {
//        float noise = 1. - texture(Noise, noiseCoord * 0.001).r;
//        color = vec4(mix(mix(light, dark, noise), color.rgb, color.a * 0.5 + 0.5), 1.);
//    }

    fragColor = vec4(color.rgb, 1.0);
}