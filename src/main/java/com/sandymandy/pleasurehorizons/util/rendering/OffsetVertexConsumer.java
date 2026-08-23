package com.sandymandy.pleasurehorizons.util.rendering;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Wraps a {@link VertexConsumer} and shifts every texture coordinate that passes through it.
 *
 * <p>Used to pick which armour material a girl's baked armour bones are drawn with: the armour
 * atlas stores the materials as columns, so switching material is a horizontal UV shift rather
 * than a different texture.</p>
 *
 * <p>1.21.1 renamed the whole interface ({@code vertex} to {@code addVertex}, {@code texture}
 * to {@code setUv}, {@code overlay}/{@code light} to {@code setUv1}/{@code setUv2}), so only
 * the six abstract methods are overridden here and everything else is inherited as a default.
 * The port previously shipped this as a class that compiled but shifted nothing.</p>
 */
public class OffsetVertexConsumer implements VertexConsumer {
    private VertexConsumer delegate;
    private float uOffset;
    private float vOffset;

    /**
     * Points this wrapper at a buffer. If the target is itself an offset wrapper its
     * underlying buffer is taken instead, so offsets can never stack up by accident.
     */
    public void setup(VertexConsumer delegate, float uOffset, float vOffset) {
        if (delegate instanceof OffsetVertexConsumer offset) {
            this.delegate = offset.getDelegate();
        } else {
            this.delegate = delegate;
        }
        this.uOffset = uOffset;
        this.vOffset = vOffset;
    }

    public VertexConsumer getDelegate() {
        return this.delegate;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        this.delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        this.delegate.setColor(red, green, blue, alpha);
        return this;
    }

    /** The whole point of this class. */
    @Override
    public VertexConsumer setUv(float u, float v) {
        this.delegate.setUv(u + this.uOffset, v + this.vOffset);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        this.delegate.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        this.delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        this.delegate.setNormal(x, y, z);
        return this;
    }
}
