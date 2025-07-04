#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform float XOffset;

in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler1, texCoord0);
    if(color.a == 0) {
        discard;
    }

    vec2 samplePos = color.rg + vec2(XOffset, 0.0);
    vec4 outColor = texture(Sampler0, samplePos);
    if(outColor.a == 0) {
        discard;
    }

    fragColor = outColor;
}