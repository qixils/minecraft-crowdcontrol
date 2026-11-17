#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:globals.glsl>

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform DoubleVisionConfig {
    vec2 Separation;
    float DriftSpeed;
};

layout(location = 0) out vec4 fragColor;

const float TAU = 6.2831853;

void main() {
    // the two images drift around each other on slightly mismatched periods, so they never settle
    float phase = GameTime * DriftSpeed * TAU;
    vec2 offset = Separation * vec2(sin(phase), cos(phase * 0.73));

    vec3 left = texture(InSampler, texCoord + offset).rgb;
    vec3 right = texture(InSampler, texCoord - offset).rgb;

    fragColor = vec4((left + right) * 0.5, 1.0);
}
