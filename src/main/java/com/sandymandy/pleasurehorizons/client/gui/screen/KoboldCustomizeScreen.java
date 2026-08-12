package com.sandymandy.pleasurehorizons.client.gui.screen;

import com.sandymandy.pleasurehorizons.client.gui.screen.customize.ButtonGridSection;
import com.sandymandy.pleasurehorizons.client.gui.screen.customize.CustomizeSection;
import com.sandymandy.pleasurehorizons.client.gui.screen.customize.SliderSection;
import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import com.sandymandy.pleasurehorizons.entity.girls.KoboldEntity;
import com.sandymandy.pleasurehorizons.networking.C2S.KoboldCustomizeC2SPacket;
import com.sandymandy.pleasurehorizons.util.Colors;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.sandymandy.pleasurehorizons.util.Utils.getFormattedByUnderscore;

public class KoboldCustomizeScreen extends CustomizeScreen<KoboldEntity> {

    private int bodySize;
    private int breastSize;
    private int primaryColor;
    private int secondaryColor;
    private int irisColor;
    private int topHornType;
    private int bottomHornType;

    public KoboldCustomizeScreen(Component title, int entityId, int previewEntityId) {
        super(title, entityId, previewEntityId, KoboldEntity.class);

        if (entity != null) {
            this.bodySize = entity.getBodySize();
            this.breastSize = entity.getKoboldBreastSize();
            this.primaryColor = entity.getPrimaryColor();
            this.secondaryColor = entity.getSecondaryColor();
            this.irisColor = entity.getIrisColor();
            this.topHornType = entity.getTopHornType();
            this.bottomHornType = entity.getBottomHornType();
        } else {
            this.bodySize = 100;
            this.breastSize = 100;
            this.primaryColor = Colors.PEACH;
            this.secondaryColor = Colors.BANANA;
            this.irisColor = Colors.SKY_BLUE;
            this.topHornType = 0;
            this.bottomHornType = 0;
        }
    }

    public KoboldCustomizeScreen(int entityId, int previewEntityId) {
        this(Component.translatable("gui.pleasurehorizons.customize.titleKobold"), entityId, previewEntityId);
    }

    @Override
    protected void addSections() {
        if (entity == null) return;

        sections.add(new SliderSection<>(
                entity, previewEntity,
                Component.translatable("gui.pleasurehorizons.customize.bodySize"), 65, 115,
                () -> bodySize,
                value -> bodySize = value,
                "Size affects hitbox height"
        ));

        sections.add(new SliderSection<>(
                entity, previewEntity,
                Component.translatable("gui.pleasurehorizons.customize.breastSize"), 60, 160,
                () -> breastSize,
                value -> breastSize = value,
                "Adjust breast size"
        ));

        sections.add(new ButtonGridSection<>(
                entity, previewEntity,
                Component.translatable("gui.pleasurehorizons.customize.colorPattern"),
                "color_preset",
                KoboldEntity.PatternPresets.values(),
                2,
                preset -> Component.literal(getFormattedByUnderscore(preset.name())),
                preset -> {
                    primaryColor = preset.primary;
                    secondaryColor = preset.secondary;
                },
                () -> {
                    for (KoboldEntity.PatternPresets preset : KoboldEntity.PatternPresets.values()) {
                        if (preset.primary == primaryColor && preset.secondary == secondaryColor) {
                            return preset;
                        }
                    }
                    return null;
                }
        ));

        Integer[] irisColors = {
                Colors.SKY_BLUE, Colors.GREEN, Colors.RED,
                Colors.PURPLE, Colors.ORANGE, Colors.YELLOW,
                Colors.PINK, Colors.CYAN, Colors.LIME,
                Colors.WHITE, Colors.GRAY, Colors.BLACK
        };

        sections.add(new ButtonGridSection<>(
                entity, previewEntity,
                Component.translatable("gui.pleasurehorizons.customize.irisColor"),
                "iris_color",
                irisColors,
                3,
                color -> Component.literal("■").withStyle(style -> style.withColor(color)),
                color -> irisColor = color,
                () -> irisColor
        ));

        Integer[] topHornTypes = {0, 1, 2, 3, 4, 5, 6, 7};
        sections.add(new ButtonGridSection<>(
                entity, previewEntity,
                Component.translatable("gui.pleasurehorizons.customize.topHorns"),
                "top_horn",
                topHornTypes,
                4,
                type -> Component.translatable("gui.pleasurehorizons.customize.hornType", type),
                type -> topHornType = type,
                () -> topHornType
        ));

        Integer[] bottomHornTypes = {0, 1, 2};
        sections.add(new ButtonGridSection<>(
                entity, previewEntity,
                Component.translatable("gui.pleasurehorizons.customize.bottomHorns"),
                "bottom_horn",
                bottomHornTypes,
                3,
                type -> Component.translatable("gui.pleasurehorizons.customize.hornType", type),
                type -> bottomHornType = type,
                () -> bottomHornType
        ));

        sections.add(new CustomizeSection<KoboldEntity>(entity, previewEntity) {
            @Override
            public void init(CustomizeScreen<KoboldEntity> screen, LayoutConfig layout, int startY) {
            }

            @Override
            public int render(CustomizeScreen<KoboldEntity> screen, LayoutConfig layout, int currentY) {
                Button randomizeBtn = Button.builder(
                        Component.translatable("gui.pleasurehorizons.customize.randomize"),
                        button -> randomizeAll()
                ).bounds(layout.centerX, currentY, layout.contentWidth, 20).build();
                screen.addWidget(randomizeBtn);
                return currentY + 25;
            }
        });
    }

    private void randomizeAll() {
        bodySize = GirlEntity.RANDOM.nextInt(65, 116);
        breastSize = GirlEntity.RANDOM.nextInt(60, 161);

        KoboldEntity.PatternPresets preset = KoboldEntity.PatternPresets.values()[
                GirlEntity.RANDOM.nextInt(KoboldEntity.PatternPresets.values().length)
                ];
        primaryColor = preset.primary;
        secondaryColor = preset.secondary;

        Integer[] colors = {
                Colors.SKY_BLUE, Colors.GREEN, Colors.RED,
                Colors.PURPLE, Colors.ORANGE, Colors.YELLOW,
                Colors.PINK, Colors.CYAN, Colors.LIME,
                Colors.WHITE, Colors.GRAY, Colors.BLACK
        };
        irisColor = colors[GirlEntity.RANDOM.nextInt(colors.length)];

        topHornType = GirlEntity.RANDOM.nextInt(0, 8);
        bottomHornType = GirlEntity.RANDOM.nextInt(0, 3);

        init();
    }

    @Override
    protected void applyToPreview() {
        if (previewEntity == null) return;
        previewEntity.setBodySize(bodySize);
        previewEntity.setKoboldBreastSize(breastSize);
        previewEntity.setPrimaryColor(primaryColor);
        previewEntity.setSecondaryColor(secondaryColor);
        previewEntity.setIrisColor(irisColor);
        previewEntity.setTopHornType(topHornType);
        previewEntity.setBottomHornType(bottomHornType);
    }

    @Override
    protected void onConfirm() {
        PacketDistributor.sendToServer(new KoboldCustomizeC2SPacket(
                entityId, bodySize, breastSize,
                primaryColor, secondaryColor, irisColor,
                topHornType, bottomHornType
        ));
    }

    @Override
    protected int contentHeight() {
        return 550;
    }
}
