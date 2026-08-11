package com.sandymandy.pleasurehorizons.config.keys;

import java.util.function.Consumer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.texture.TextureTickListener;
import net.minecraft.client.util.InputUtil;

public class FreecamKeyMapping extends KeyBinding implements TextureTickListener {

    private final Consumer<FreecamKeyMapping> onTick;

    /**
     * @apiNote should only be used if overriding {@link #tick()}
     */
    protected FreecamKeyMapping(String translationKey, InputUtil.Type type, int code) {
        this(translationKey, type, code, null);
    }

    FreecamKeyMapping(String translationKey, InputUtil.Type type, int code, Consumer<FreecamKeyMapping> onTick) {
        super("key.freecam." + translationKey, type, code, "key.categories.freecam");
        this.onTick = onTick;
    }

    @Override
    public void tick() {
        onTick.accept(this);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    public void reset() {
        while (wasPressed()) {}
    }
}
