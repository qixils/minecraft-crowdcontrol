#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:globals.glsl>
#include <crowdcontrol:depth.glsl>

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform SonarConfig {
    float NearPlane;
    float Ambient;
    float SweepStart;
    float SweepRange;
    float SweepWidth;
    float SweepSpeed;
    float FadeIn;
    float FadeOut;
};

layout(location = 0) out vec4 fragColor;

void main() {
    float distance = ccDepthToDistance(texture(DepthSampler, texCoord).r, NearPlane);

    // A shell of light expands away from the player on a loop, lighting up only the geometry it is
    // currently passing through. Unlike blindness this hides things by *range* rather than by
    // radius, so a wall three blocks away and one thirty blocks away are never lit at once.
    float phase = fract(GameTime * SweepSpeed);
    float ping = mix(SweepStart, SweepRange, phase);
    float band = 1.0 - smoothstep(0.0, SweepWidth, abs(distance - ping));

    // Each ping launches from behind the player and swells in, then dies away as it reaches the far
    // end, so the band is invisible at the moment it wraps back around instead of snapping from the
    // horizon to the player's feet.
    float strength = smoothstep(0.0, FadeIn, phase) * (1.0 - smoothstep(1.0 - FadeOut, 1.0, phase));

    // the ambient floor keeps the player from being outright blind between pings
    float visibility = Ambient + band * strength * (1.0 - Ambient);

    fragColor = vec4(texture(InSampler, texCoord).rgb * visibility, 1.0);
}
