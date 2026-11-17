#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(location = 0) out vec4 fragColor;

void main() {
    vec2 oneTexel = 1.0 / InSize;

    vec4 center = texture(InSampler, texCoord);
    vec4 left   = texture(InSampler, texCoord - vec2(oneTexel.x, 0.0));
    vec4 right  = texture(InSampler, texCoord + vec2(oneTexel.x, 0.0));
    vec4 up     = texture(InSampler, texCoord - vec2(0.0, oneTexel.y));
    vec4 down   = texture(InSampler, texCoord + vec2(0.0, oneTexel.y));
    vec4 leftDiff  = center - left;
    vec4 rightDiff = center - right;
    vec4 upDiff    = center - up;
    vec4 downDiff  = center - down;
    vec4 total = clamp(leftDiff + rightDiff + upDiff + downDiff, 0.0, 1.0);
    fragColor = vec4(total.rgb, 1.0);
}
