package com.sandymandy.pleasurehorizons.util.rendering;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class OffsetVertexConsumer implements VertexConsumer {
    private final VertexConsumer parent;
    public OffsetVertexConsumer(VertexConsumer parent) { this.parent = parent; }
    public OffsetVertexConsumer() { this.parent = null; }
    public void setup(VertexConsumer delegate, float u, float v) {}
    public VertexConsumer getDelegate() { return parent; }
    public VertexConsumer addVertex(float x, float y, float z) { return this; }
    public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
    public VertexConsumer setUv(float u, float v) { return this; }
    public VertexConsumer setUv2(int u, int v) { return this; }
    public VertexConsumer setUv1(int u, int v) { return this; }
    public VertexConsumer setNormal(float x, float y, float z) { return this; }
    // Legacy methods for older MCP mappings
    public VertexConsumer vertex(float x, float y, float z) { return this; }
    public VertexConsumer color(int r, int g, int b, int a) { return this; }
    public VertexConsumer uv(float u, float v) { return this; }
    public VertexConsumer overlayCoords(int u, int v) { return this; }
    public VertexConsumer uv2(int u, int v) { return this; }
    public VertexConsumer normal(float x, float y, float z) { return this; }
    public void endVertex() {}
    public void defaultColor(int r, int g, int b, int a) {}
    public void unsetDefaultColor() {}
}
