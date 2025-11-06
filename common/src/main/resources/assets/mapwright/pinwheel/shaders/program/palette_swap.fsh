#version 150

uniform sampler2D Sampler0;
uniform sampler2D Palette;

in vec2 texCoord0;

out vec4 fragColor;

vec3 samplePalette(float x) {
    return texture(Palette, vec2(x, 0.5)).rgb;
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if(color.a == 0) {
        discard;
    }

    float average = (color.r + color.g + color.b) / 3.0f;
    float offsetToAccountForTheRealKnowledge = 0.005;
    vec3 outColor = samplePalette(average + offsetToAccountForTheRealKnowledge);

    fragColor = vec4(outColor, 1.0);
}