#ifndef CROWDCONTROL_DEPTH_GLSL
#define CROWDCONTROL_DEPTH_GLSL

// Minecraft draws the world with a reversed-Z projection: net.minecraft.client.renderer.Projection
// feeds JOML's setPerspective the far plane as its near argument, and GameRenderer clears depth to
// 0.0. The depth buffer therefore holds 1.0 at the near plane and 0.0 at (or past) the far plane.
//
// Post-processing passes are handed the fullscreen quad's orthographic matrix instead of the world
// projection, and the Projection uniform block is not part of the post-processing pipeline layout at
// all, so the usual deviceToLinearDepth() from minecraft:oit_common.glsl cannot be used here.
// Distance is recovered from the near plane alone instead. Exactly:
//
//     t = (N * F) / (N + d * (F - N))
//
// which for anything comfortably inside the far plane F collapses to t = N / d, accurate to a small
// fraction of a block. N is Minecraft's fixed 0.05 near plane, passed in so it stays tunable.

// Stand-in distance for pixels nothing was drawn to. Far enough to sit past any render distance,
// small enough that differencing it against real distances cannot blow up a float.
const float CC_SKY_DISTANCE = 4096.0;

// Distance in blocks from the camera to whatever was drawn at this pixel.
float ccDepthToDistance(float deviceDepth, float nearPlane) {
    if (deviceDepth <= 0.0) return CC_SKY_DISTANCE;
    return min(nearPlane / deviceDepth, CC_SKY_DISTANCE);
}

#endif
