#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

uniform vec2 translation;
uniform float scale;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord0;
out vec2 screenCoord;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord0 = UV0;
    screenCoord = vec2(UV2);
}