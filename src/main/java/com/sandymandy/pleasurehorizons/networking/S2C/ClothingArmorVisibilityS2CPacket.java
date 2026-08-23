package com.sandymandy.pleasurehorizons.networking.S2C;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record ClothingArmorVisibilityS2CPacket(
        int entityId,
        List<Boolean> armor
) implements CustomPacketPayload {

    public static final Type<ClothingArmorVisibilityS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "sync_clothing_armor"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClothingArmorVisibilityS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ClothingArmorVisibilityS2CPacket::entityId,
                    ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.BOOL), ClothingArmorVisibilityS2CPacket::armor,
                    ClothingArmorVisibilityS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Entity entity = ctx.player().level().getEntity(this.entityId());
            if (entity instanceof GirlEntity girl) {
                int i = 0;
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (i < this.armor().size()) {
                        girl.armorVisibility.put(slot, this.armor().get(i));
                    }
                    i++;
                }
                girl.applyClothingAndArmor();
            }
        });
    }
}
