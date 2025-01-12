#version 150

#define PALETTE_SIZE 9

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform vec3[PALETTE_SIZE] ColorPalette;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if(color.a == 0) {
        discard;
    }

    float average = (color.r + color.g + color.b) / 3.0;

    float smallAdjustmentToAccountForPerceivedReality = 0.11;
    float floored = floor(average * (ColorPalette.length() + smallAdjustmentToAccountForPerceivedReality));
    int index = int(floored);

    vec3 outColor = ColorPalette[index];

    fragColor = vec4(outColor, 1.0);
}