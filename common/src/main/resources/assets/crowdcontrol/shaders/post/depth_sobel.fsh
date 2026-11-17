#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D DepthSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 DepthSize;
};

layout(std140) uniform BlueprintConfig {
    float EdgeThreshold;
    vec3 LineColor;
    vec3 BackgroundColor;
};

layout(location = 0) out vec4 fragColor;

// Edges come from the second derivative of *device* depth rather than of distance. Perspective
// makes device depth vary linearly across a flat surface, so this is exactly zero on walls and
// floors no matter how steeply they are angled, and only lights up on creases and silhouettes.
// See crowdcontrol:depth.glsl for the depth convention this relies on.
void main() {
    vec2 oneTexel = 1.0 / DepthSize;

    float c = texture(DepthSampler, texCoord).r;
    float l = texture(DepthSampler, texCoord - vec2(oneTexel.x, 0.0)).r;
    float r = texture(DepthSampler, texCoord + vec2(oneTexel.x, 0.0)).r;
    float u = texture(DepthSampler, texCoord - vec2(0.0, oneTexel.y)).r;
    float d = texture(DepthSampler, texCoord + vec2(0.0, oneTexel.y)).r;

    float curvature = abs(l + r - 2.0 * c) + abs(u + d - 2.0 * c);

    // scaling the threshold by depth keeps a given real-world crease equally visible at any range
    float scale = max(c, 1.0e-5);
    float edge = smoothstep(EdgeThreshold * scale, EdgeThreshold * scale * 6.0, curvature);

    fragColor = vec4(mix(BackgroundColor, LineColor, edge), 1.0);
}
