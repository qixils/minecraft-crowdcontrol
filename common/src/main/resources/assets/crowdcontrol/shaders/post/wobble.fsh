#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:globals.glsl>

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform WobbleConfig {
    vec2 Frequency;
    vec2 WobbleAmount;
    float HueSpeed;
};

layout(location = 0) out vec4 fragColor;

// Globals.GameTime wraps once every 24000 ticks; 1200 wraps of that span back
// into the one-per-20-ticks ramp that the legacy `Time` uniform provided.
const float TIME_CYCLES_PER_DAY = 1200.0;

vec3 hue(float h) {
    float r = abs(h * 6.0 - 3.0) - 1.0;
    float g = 2.0 - abs(h * 6.0 - 2.0);
    float b = 2.0 - abs(h * 6.0 - 4.0);
    return clamp(vec3(r, g, b), 0.0, 1.0);
}

vec3 HSVtoRGB(vec3 hsv) {
    return ((hue(hsv.x) - 1.0) * hsv.y + 1.0) * hsv.z;
}

vec3 RGBtoHSV(vec3 rgb) {
    vec3 hsv = vec3(0.0);
    hsv.z = max(rgb.r, max(rgb.g, rgb.b));
    float minComponent = min(rgb.r, min(rgb.g, rgb.b));
    float c = hsv.z - minComponent;

    if (c != 0.0) {
        hsv.y = c / hsv.z;
        vec3 delta = (hsv.z - rgb) / c;
        delta.rgb -= delta.brg;
        delta.rg += vec2(2.0, 4.0);
        if (rgb.r >= hsv.z) {
            hsv.x = delta.b;
        } else if (rgb.g >= hsv.z) {
            hsv.x = delta.r;
        } else {
            hsv.x = delta.g;
        }
        hsv.x = fract(hsv.x / 6.0);
    }
    return hsv;
}

void main() {
    float Time = fract(GameTime * TIME_CYCLES_PER_DAY);

    float xOffset = sin(texCoord.y * Frequency.x + Time * 3.1415926535 * 2.0) * WobbleAmount.x;
    float yOffset = cos(texCoord.x * Frequency.y + Time * 3.1415926535 * 2.0) * WobbleAmount.y;
    vec2 offset = vec2(xOffset, yOffset);
    vec4 rgb = texture(InSampler, texCoord + offset);
    vec3 hsv = RGBtoHSV(rgb.rgb);
    // Deliberately much slower than vanilla's one-full-rotation-per-second to
    // limit the strobing that made the original a photosensitivity hazard.
    hsv.x = fract(hsv.x + GameTime * HueSpeed);
    fragColor = vec4(HSVtoRGB(hsv), 1.0);
}
