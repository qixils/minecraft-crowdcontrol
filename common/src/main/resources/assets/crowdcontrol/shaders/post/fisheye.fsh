#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform FisheyeConfig {
    float CenterStrength;
    float EdgeStrength;
};

layout(location = 0) out vec4 fragColor;

void main() {
    // work in aspect-corrected space so the distortion stays circular on any screen
    float aspect = InSize.x / InSize.y;
    vec2 centered = (texCoord - 0.5) * vec2(aspect, 1.0);

    float maxRadius = length(vec2(aspect, 1.0) * 0.5);
    float radius = length(centered) / maxRadius;

    // Barrel distortion ramped linearly from the middle outwards rather than quadratically, so the
    // magnification is felt across the whole frame instead of only in the corners. Sampling always
    // lands inside the frame, so no clamping smear appears at the edges.
    float strength = mix(CenterStrength, EdgeStrength, radius);

    vec2 sampled = centered * (1.0 - strength);
    vec2 uv = sampled / vec2(aspect, 1.0) + 0.5;

    fragColor = vec4(texture(InSampler, uv).rgb, 1.0);
}
