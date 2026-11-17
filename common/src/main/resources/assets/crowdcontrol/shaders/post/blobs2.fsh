#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform BlobsConfig {
    float Radius;
};

layout(location = 0) out vec4 fragColor;

void main() {
    vec2 oneTexel = 1.0 / InSize;

    vec4 c = texture(InSampler, texCoord);
    vec4 maxVal = c;
    for (float u = 0.0; u <= Radius; u += 1.0) {
        for (float v = 0.0; v <= Radius; v += 1.0) {
            float weight = (((sqrt(u * u + v * v) / Radius) > 1.0) ? 0.0 : 1.0);

            vec4 s0 = texture(InSampler, texCoord + vec2(-u * oneTexel.x, -v * oneTexel.y));
            vec4 s1 = texture(InSampler, texCoord + vec2( u * oneTexel.x,  v * oneTexel.y));
            vec4 s2 = texture(InSampler, texCoord + vec2(-u * oneTexel.x,  v * oneTexel.y));
            vec4 s3 = texture(InSampler, texCoord + vec2( u * oneTexel.x, -v * oneTexel.y));

            vec4 o0 = max(s0, s1);
            vec4 o1 = max(s2, s3);
            vec4 tempMax = max(o0, o1);
            maxVal = mix(maxVal, max(maxVal, tempMax), weight);
        }
    }

    fragColor = vec4(maxVal.rgb, 1.0);
}
