#version 150

uniform sampler2D Sampler0;
uniform sampler2D Palette;

in vec2 texCoord0;
in vec2 noiseCoord;

out vec4 fragColor;

// https://www.shadertoy.com/view/MstGRl
// Determines how many cells there are
#define NUM_CELLS 16.0

// Arbitrary random, can be replaced with a function of your choice
float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

// Returns the point in a given cell
vec2 get_cell_point(ivec2 cell) {
    vec2 cell_base = vec2(cell) / NUM_CELLS;
    float noise_x = rand(vec2(cell));
    float noise_y = rand(vec2(cell.yx));
    return cell_base + (0.5 + 1.5 * vec2(noise_x, noise_y)) / NUM_CELLS;
}

// Performs worley noise by checking all adjacent cells
// and comparing the distance to their points
float worley(vec2 coord) {
    ivec2 cell = ivec2(coord * NUM_CELLS);
    float dist = 1.0;

    // Search in the surrounding 5x5 cell block
    for (int x = 0; x < 5; x++) {
        for (int y = 0; y < 5; y++) {
            vec2 cell_point = get_cell_point(cell + ivec2(x-2, y-2));
            dist = min(dist, distance(cell_point, coord));

        }
    }

    dist /= length(vec2(1.0 / NUM_CELLS));
    dist = 1.0 - dist;
    return dist;
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if(color.a == 0) {
        discard;
    }
    if (color == vec4(1.,0.,1.,1.)) {
        float noise = worley(floor(noiseCoord) * 0.01); // todo proper noise
        color = vec4(vec3(noise * 0.25 + 0.75),1.);
    }

    fragColor = vec4(color.rgb, 1.0);
}