#version 330
#extension GL_ARB_separate_shader_objects : require

layout(location = 0) out vec2 texCoord;

// Mirrors the whole frame vertically by flipping the sampled coordinate rather
// than the geometry, so every layer of the main target (sky included) is flipped.
void main() {
    vec2 uv = vec2((gl_VertexIndex << 1) & 2, gl_VertexIndex & 2);
    vec4 pos = vec4(uv * vec2(2, 2) + vec2(-1, -1), 0, 1);

    gl_Position = pos;
    texCoord = vec2(uv.x, 1.0 - uv.y);
}
