package com.sandymandy.pleasurehorizons.client.gui.screen;

import com.sandymandy.pleasurehorizons.client.gui.screen.customize.*;
import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import com.sandymandy.pleasurehorizons.entity.girls.KoboldEntity;
import com.sandymandy.pleasurehorizons.networking.C2S.KoboldCustomizeC2SPacket;
import com.sandymandy.pleasurehorizons.util.Colors;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.chat.Component;

import static com.sandymandy.pleasurehorizons.util.Utils.getFormattedByUnderscore;

public class KoboldCustomizeScreen extends CustomizeScreen<KoboldEntity> {

    private int bodySize;
    private int breastSize;
    private int primaryColor;
    private int secondaryColor;
    private int irisColor;
    private int topHornType;
    private int bottomHornType;

    public KoboldCustomizeScreen(int entityId, int previewEntityId) {
        super(Text.literal("§6§lKobold Customization"), entityId, previewEntityId, KoboldEntity.class);

        if (entity != null) {
            this.bodySize = entity.getBodySize();
            this.breastSize = entity.getKoboldBreastSize();
            this.primaryColor = entity.getPrimaryColor();
            this.secondaryColor = entity.getSecondaryColor();
            this.irisColor = entity.getIrisColor();
            this.topHornType = entity.getTopHornType();
            this.bottomHornType = entity.getBottomHornType();
        }
    }

    @Override
    protected void addSections() {
        // Body Size Slider
        sections.add(new SliderSection<>(
                entity, previewEntity,
                "Body Size", 65, 115,
                () -> bodySize,
                value -> bodySize = value,
                "Size affects hitbox height\n65 = 1 block, 115 = 1.75 blocks"
        ));

        // Breast Size Slider
        sections.add(new SliderSection<>(
                entity, previewEntity,
                "Breast Size", 60, 160,
                () -> breastSize,
                value -> {
                    breastSize = value;
                },
                "Adjust breast size (60-160)"
        ));

        // Color Presets
        sections.add(new ButtonGridSection<>(
                entity, previewEntity,
                "§eColor Pattern:",
                "color_preset",
                KoboldEntity.PatternPresets.values(),
                2, // 2 columns
                preset -> Text.literal(getFormattedByUnderscore(preset.name())),
                preset -> {
                    primaryColor = preset.primary;
                    secondaryColor = preset.secondary;
                },
                () -> {
                    // Find current preset
                    for (KoboldEntity.PatternPresets preset : KoboldEntity.PatternPresets.values()) {
                        if (preset.primary == primaryColor && preset.secondary == secondaryColor) {
                            return preset;
                        }
                    }
                    return null;
                }
        ));

        // Iris Color
        Integer[] irisColors = {
                Colors.SKY_BLUE, Colors.GREEN, Colors.RED,
                Colors.PURPLE, Colors.ORANGE, Colors.YELLOW,
                Colors.PINK, Colors.CYAN, Colors.LIME,
                Colors.WHITE, Colors.GRAY, Colors.BLACK
        };

        sections.add(new ButtonGridSection<>(
                entity, previewEntity,
                "§eIris Color:",
                "iris_color",
                irisColors,
                3, // 3 columns
                color -> Text.literal("■").styled(style -> style.withColor(color)),
                color -> {
                    irisColor = color;
                },
                () -> irisColor
        ));

        // Top Horns
        Integer[] topHornTypes = {0, 1, 2, 3, 4, 5, 6, 7};
        sections.add(new ButtonGridSection<>(
                entity, previewEntity,
                "§eTop Horns:",
                "top_horn",
                topHornTypes,
                4, // 4 columns
                type -> Text.literal("Type " + type),
                type -> {
                    topHornType = type;
                },
                () -> topHornType
        ));

        // Bottom Horns
        Integer[] bottomHornTypes = {0, 1, 2};
        sections.add(new ButtonGridSection<>(
                entity, previewEntity,
                "§eBottom Horns:",
                "bottom_horn",
                bottomHornTypes,
                3, // 3 columns
                type -> Text.literal("Type " + type),
                type -> {
                    bottomHornType = type;
                },
                () -> bottomHornType
        ));

        // Randomize Button
        sections.add(new CustomizeSection<KoboldEntity>(entity, previewEntity) {
            @Override
            public void init(CustomizeScreen<KoboldEntity> screen, LayoutConfig layout, int startY) {
            }

            @Override
            public int render(CustomizeScreen<KoboldEntity> screen, LayoutConfig layout, int currentY) {
                ButtonWidget randomizeBtn = ButtonWidget.builder(
                        Text.literal("§d§lRandomize"),
                        button -> randomizeAll()
                ).dimensions(layout.centerX, currentY, layout.contentWidth, 20).build();

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

        init(); // Refresh UI to show new selections
    }

    @Override
    protected void applyToPreview() {
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
