#version 330
#extension GL_ARB_separate_shader_objects : require

#include <crowdcontrol:depth.glsl>

uniform sampler2D InSampler;
uniform sampler2D BlurSampler;
uniform sampler2D DepthSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform MyopiaConfig {
    float NearPlane;
    float FocusDistance;
    float BlurDistance;
};

layout(location = 0) out vec4 fragColor;

void main() {
    float distance = ccDepthToDistance(texture(DepthSampler, texCoord).r, NearPlane);
    float blurAmount = smoothstep(FocusDistance, BlurDistance, distance);

    vec3 sharp = texture(InSampler, texCoord).rgb;
    vec3 blurred = texture(BlurSampler, texCoord).rgb;

    fragColor = vec4(mix(sharp, blurred, blurAmount), 1.0);
}
