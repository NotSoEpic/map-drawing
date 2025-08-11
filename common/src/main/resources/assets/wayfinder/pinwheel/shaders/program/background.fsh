#version 150

uniform sampler2D Sampler0;
uniform sampler2D Palette;

uniform vec2 translation;
uniform float scale;

in vec2 texCoord0;
in vec2 screenCoord;

out vec4 fragColor;


void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if(color.a == 0) {
        discard;
    }
    if (color == vec4(1.,0.,1.,1.)) {
        vec2 pos = mod(screenCoord, 100.) / 100.;
        color = vec4(pos.x,pos.y,1.,1.);
    }

    fragColor = vec4(color.rgb, 1.0);
}