#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;
uniform sampler2D PrevSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform PhosphorConfig {
    vec3 Phosphor;
};

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 CurrTexel = texture(InSampler, texCoord);
    vec4 PrevTexel = texture(PrevSampler, texCoord);

    fragColor = vec4(max(PrevTexel.rgb * Phosphor, CurrTexel.rgb), 1.0);
}
