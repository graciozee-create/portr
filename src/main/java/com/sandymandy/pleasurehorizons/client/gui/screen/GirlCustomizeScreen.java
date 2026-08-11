package com.sandymandy.pleasurehorizons.client.gui.screen;

import com.sandymandy.pleasurehorizons.client.gui.screen.customize.ButtonSection;
import com.sandymandy.pleasurehorizons.client.gui.screen.customize.CustomizeSection;
import com.sandymandy.pleasurehorizons.client.gui.screen.customize.SliderSection;
import com.sandymandy.pleasurehorizons.client.gui.screen.customize.Vec3dInputSection;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.networking.C2S.GirlCustomizeC2SPacket;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.Vec3;

public class GirlCustomizeScreen extends CustomizeScreen<GirlSceneEntity> {

    private int breastSize;
    private boolean canGetImpregnated;
    private Vec3d breastOffset;

    public GirlCustomizeScreen(int entityId, int previewEntityId) {
        super(Component.literal("Customize Girl"), entityId, previewEntityId, GirlSceneEntity.class);
        entity.setGUIOpenState(true);

        if (entity != null) {
            this.breastSize = entity.getBreastSize();
            this.breastOffset = entity.getBreastOffset();
            this.canGetImpregnated = entity.canGetImpregnated();
        }
    }

    @Override
    protected void addSections() {
        //Breast Size Slider
        sections.add(new SliderSection<>(
                entity, previewEntity,
                "Breast Size", entity.getBreastMinSize(), entity.getBreastMaxSize(),
                () -> breastSize,
                value -> breastSize = value,
                ""
                ));

        //Breast Offset
        sections.add(new Vec3dInputSection<>(
                entity, previewEntity,
                "Breast Offset",
                () -> breastOffset,
                vec3d -> breastOffset = vec3d
        ));

        // Can Get Impregnated
        sections.add(new ButtonSection<>(
                entity, previewEntity,
                "Can Get Impegnated",
                () -> canGetImpregnated,
                value -> canGetImpregnated = value
        ));

        //Clear
        sections.add(new CustomizeSection<GirlSceneEntity>(entity, previewEntity) {
            @Override
            public void init(CustomizeScreen<GirlSceneEntity> screen, LayoutConfig layout, int startY) {
            }

            @Override
            public int render(CustomizeScreen<GirlSceneEntity> screen, LayoutConfig layout, int currentY) {
                ButtonWidget randomizeBtn = ButtonWidget.builder(
                        Component.literal("Clear").formatted(Formatting.RED, Formatting.BOLD),
                        button -> {
                            onClear();
                            screen.close();
                        }
                ).dimensions(layout.centerX, currentY, layout.contentWidth, 20).build();

                screen.addWidget(randomizeBtn);
                return currentY + 25;
            }
        });    }

    public void onClear() {
        PacketDistributor.sendToServer(new GirlCustomizeC2SPacket(
                this.entityId,
                100,
                new Vec3d(0, 0, 0),
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
        previewEntity.setBreastSize(breastSize);
        previewEntity.setBreastOffset(breastOffset);
    }

    @Override
    protected int contentHeight() {
        return 400;
    }

}
