#version 150

#define PALETTE_SIZE 9

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float[PALETTE_SIZE * 3] ColorPalette;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor;

    if(color.a == 0) {
        discard;
    }

    float average = (color.r + color.g + color.b) / 3.0;

    float smallAdjustmentToAccountForPercievedReality = 0.11;
    int i = int(floor(average * (float(PALETTE_SIZE) + smallAdjustmentToAccountForPercievedReality))) * 3;

    vec3 outColor = vec3(ColorPalette[i], ColorPalette[i + 1], ColorPalette[i + 2]);

    fragColor = vec4(outColor, 1.0);
}