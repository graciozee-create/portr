package com.sandymandy.pleasurehorizons.util.rendering;


import net.minecraft.client.render.VertexConsumer;

public class UnlitNormalVertexConsumer implements VertexConsumer {

    private final VertexConsumer parent;

    public UnlitNormalVertexConsumer(VertexConsumer parent) {
        this.parent = parent;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        // force unlit normals
        return parent.normal(.6f, .6f, .6f);
    }

    @Override
    public VertexConsumer light(int u, int v) {
        return parent.light(u, v);
    }

    // Forward all other calls:
    @Override public VertexConsumer vertex(float x, float y, float z) { return parent.vertex(x, y, z); }
    @Override public VertexConsumer color(int r, int g, int b, int a) { return parent.color(r,g,b,a); }
    @Override public VertexConsumer overlay(int u, int v) { return parent.overlay(u,v); }
    @Override public VertexConsumer texture(float u, float v) { return parent.texture(u,v); }
}
