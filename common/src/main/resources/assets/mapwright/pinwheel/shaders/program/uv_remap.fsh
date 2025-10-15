#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform float XOffset;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler1, texCoord0);
    if(color.a == 0) {
        discard;
    }

    if(color == vec4(0.0, 0.0, 0.0, 1.0)) {
        fragColor = color * vertexColor;
    } else {
        vec2 samplePos = color.rg + vec2(XOffset, 0.0);
        color = texture(Sampler0, samplePos) * vertexColor;

        if(color.a == 0) {
            discard;
        }

        fragColor = color;
    }
}