package com.sandymandy.pleasurehorizons.client.gui.screen;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.networking.C2S.SetGUIOpenStateC2SPacket;
import com.sandymandy.pleasurehorizons.registries.InventoryButtonRegistry;
import com.sandymandy.pleasurehorizons.screen.GirlInventoryScreenHandler;
import com.sandymandy.pleasurehorizons.screen.InventoryButtonAction;
import com.sandymandy.pleasurehorizons.util.ScreenUtils;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Colors;
import net.minecraft.resources.ResourceLocation;

import static com.sandymandy.pleasurehorizons.util.PleasureHorizonsIcons.*;

public class GirlInventoryScreen extends HandledScreen<GirlInventoryScreenHandler> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "/textures/gui/inventory.png");
    private float xMouse;
    private float yMouse;
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 170;
    private final TameableGirlEntity girl;
    private final Player player;


    public GirlInventoryScreen(GirlInventoryScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.girl = handler.getGirl();
        this.player = inventory.player;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int alpha = 120; // adjust blur opacity
        super.render(context, mouseX, mouseY, delta);
//        drawMouseoverTooltip(context,mouseX,mouseY);

    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        //Stops the container names from rendering
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int centerX = (width - GUI_WIDTH) / 2;
        int centerY = (height - GUI_HEIGHT) / 2;
        int i = this.x;
        int j = this.y;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, centerX, centerY, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
        InventoryScreen.drawEntity(context, i + 26, j + 8, i + 75, j + 78, this.girl.getSizeGUI(), this.girl.getYAxisGUI(), mouseX, mouseY, this.girl);

        int iconY = centerY - 22; // Positioned slightly above the top edge of the GUI
        int iconSize = 18;

        int relLevel = girl.getCurrentRelationshipLevel();
        int relMax = girl.maxRelationshipLevel();
        String relText = relLevel + "/" + relMax;
        int relX = centerX;

        context.drawTexture(RenderPipelines.GUI_TEXTURED, HEART_ICON, relX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);
        context.drawText(this.textRenderer, Component.literal(relText), relX + 20, iconY + 5, Colors.WHITE, true);

        if (ScreenUtils.isMouseOverHere(mouseX, mouseY, relX, iconY, 18, 18)) {
            context.drawTooltip(textRenderer, Component.translatable("screen.pleasurehorizons.girl_inventory.relationship_tooltip"), mouseX, mouseY);
        }

        int pregLevel = girl.getPregnancyStage();
        int pregMax = girl.maxPregnancyStage();
        int pregX = centerX + GUI_WIDTH - iconSize; // Aligned to the far right edge of the menu
        String pregText = pregLevel + "/" + pregMax;
        int textWidth = this.textRenderer.getWidth(pregText);

        if(girl.canGetImpregnated()){
            context.drawTexture(RenderPipelines.GUI_TEXTURED, getPregnancyIcon(pregLevel), pregX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);
            context.drawText(this.textRenderer, Component.literal(pregText), pregX - textWidth - 5, iconY + 5, Colors.WHITE, true);

            if (ScreenUtils.isMouseOverHere(mouseX, mouseY, pregX, iconY, 18, 18)) {
                context.drawTooltip(textRenderer, Component.translatable("screen.pleasurehorizons.girl_inventory.pregnancy_tooltip"), mouseX, mouseY);
            }
        }


    }

    private ResourceLocation getPregnancyIcon(int stage) {
        switch (stage) {
            case 1 -> {
                return PREGNANCY_LEVEL_ONE_ICON;
            }

            case 2 -> {
                return PREGNANCY_LEVEL_TWO_ICON;
            }

            case 3 -> {
                return PREGNANCY_LEVEL_THREE_ICON;
            }

            default -> {
                return PREGNANCY_LEVEL_ZERO_ICON;
            }
        }
    }

    @Override
    public void close() {
        super.close();
        PacketDistributor.sendToServer(new SetGUIOpenStateC2SPacket(this.girl.getId(),false));
    }

    private void drawButton(Component label, InventoryButtonAction action, int x, int y, int buttonWidth, int buttonHeight){

        ButtonWidget button = ButtonWidget.builder(
                label,
                btn -> {
                    if (girl != null && client != null && player != null) {
                        action.action().accept(girl, player);  // Run the button's logic
                        this.close();
                    }
                }
        ).dimensions(x, y, buttonWidth, buttonHeight).build();

        if (girl.getCurrentRelationshipLevel() < action.requiredRelationshipLevel()) {
            button.active = false; // disables and grays out
        }

        if (!button.active) {
            button.setTooltip(Tooltip.of(Component.literal("Requires Relationship Level " + action.requiredRelationshipLevel())));
        }

        this.addDrawableChild(button);
    }

    @Override
    protected void init() {
        super.init();
        int centerX = (this.width - GUI_WIDTH) / 2;
        int centerY = (this.height - GUI_HEIGHT) / 2;

        int buttonHeight = 22;
        int buttonWidth = 80;

        int paddingX = 10;
        int paddingY = 4;

        int startX = centerX - (buttonWidth + paddingX);
        int startY = centerY + 15;

        if (girl.isTamed()){
            for (int i = 0; i < InventoryButtonRegistry.BUTTONS_LEFT.size(); i++) {
                InventoryButtonAction action = InventoryButtonRegistry.BUTTONS_LEFT.get(i);
                int y = startY + i * (buttonHeight + paddingY);
                Component dynamicLabel = action.label();



                this.drawButton(dynamicLabel, action, startX, y, buttonWidth, buttonHeight);
            }

            for (int i = 0; i < InventoryButtonRegistry.BUTTONS_RIGHT.size(); i++) {
                InventoryButtonAction action = InventoryButtonRegistry.BUTTONS_RIGHT.get(i);
                int y = startY + i * (buttonHeight + paddingY);
                Component dynamicLabel = action.label();

                if (action.label().getString().equals("Sit") && girl.isSitting()){
                    dynamicLabel = Component.literal("Stand");
                }
                else if (action.label().getString().equals("Follow Me") && girl.isFollowing()){
                    dynamicLabel = Component.literal("Stop Following");
                }

                if (action.label().getString().equals("Strip") && girl.isStripped()) {
                    dynamicLabel = Component.literal("Dress Up");
                }

                this.drawButton(dynamicLabel, action, centerX + 176 + paddingX, y, buttonWidth, buttonHeight);
            }
        }

    }

}
