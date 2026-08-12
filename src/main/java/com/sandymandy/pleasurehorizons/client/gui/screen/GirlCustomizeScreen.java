package com.sandymandy.pleasurehorizons.client.gui.screen;

import com.sandymandy.pleasurehorizons.client.gui.screen.customize.ButtonSection;
import com.sandymandy.pleasurehorizons.client.gui.screen.customize.CustomizeSection;
import com.sandymandy.pleasurehorizons.client.gui.screen.customize.SliderSection;
import com.sandymandy.pleasurehorizons.client.gui.screen.customize.Vec3dInputSection;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.networking.C2S.GirlCustomizeC2SPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class GirlCustomizeScreen extends CustomizeScreen<GirlSceneEntity> {

    private int breastSize;
    private boolean canGetImpregnated;
    private Vec3 breastOffset;

    public GirlCustomizeScreen(Component title, int entityId, int previewEntityId) {
        super(title, entityId, previewEntityId, GirlSceneEntity.class);
        if (entity != null) {
            entity.setGUIOpenState(true);
            this.breastSize = entity.getBreastSize();
            this.breastOffset = entity.getBreastOffset();
            this.canGetImpregnated = entity.canGetImpregnated();
        } else {
            this.breastSize = 100;
            this.breastOffset = Vec3.ZERO;
            this.canGetImpregnated = false;
        }
    }

    public GirlCustomizeScreen(int entityId, int previewEntityId) {
        this(Component.translatable("gui.pleasurehorizons.customize.titleGirl"), entityId, previewEntityId);
    }

    @Override
    protected void addSections() {
        if (entity == null) return;

        sections.add(new SliderSection<>(
                entity, previewEntity,
                Component.translatable("gui.pleasurehorizons.customize.breastSize"),
                entity.getBreastMinSize(), entity.getBreastMaxSize(),
                () -> breastSize,
                value -> breastSize = value,
                ""
        ));

        sections.add(new Vec3dInputSection<>(
                entity, previewEntity,
                Component.translatable("gui.pleasurehorizons.customize.breastOffset"),
                () -> breastOffset,
                vec3 -> breastOffset = vec3
        ));

        sections.add(new ButtonSection<>(
                entity, previewEntity,
                Component.translatable("gui.pleasurehorizons.customize.canGetImpregnated"),
                () -> canGetImpregnated,
                value -> canGetImpregnated = value
        ));

        sections.add(new CustomizeSection<GirlSceneEntity>(entity, previewEntity) {
            @Override
            public void init(CustomizeScreen<GirlSceneEntity> screen, LayoutConfig layout, int startY) {
            }

            @Override
            public int render(CustomizeScreen<GirlSceneEntity> screen, LayoutConfig layout, int currentY) {
                Button clearBtn = Button.builder(
                        Component.translatable("gui.pleasurehorizons.customize.clear")
                                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                        button -> {
                            onClear();
                            screen.onClose();
                        }
                ).bounds(layout.centerX, currentY, layout.contentWidth, 20).build();
                screen.addWidget(clearBtn);
                return currentY + 25;
            }
        });
    }

    public void onClear() {
        PacketDistributor.sendToServer(new GirlCustomizeC2SPacket(
                this.entityId,
                100,
                new Vec3(0, 0, 0),
                this.canGetImpregnated
        ));
    }

    @Override
    protected void onConfirm() {
        PacketDistributor.sendToServer(new GirlCustomizeC2SPacket(
                this.entityId,
                this.breastSize,
                this.breastOffset,
                this.canGetImpregnated
        ));
    }

    @Override
    protected void applyToPreview() {
        if (previewEntity != null) {
            previewEntity.setBreastSize(breastSize);
            previewEntity.setBreastOffset(breastOffset);
        }
    }

    @Override
    protected int contentHeight() {
        return 400;
    }
}
