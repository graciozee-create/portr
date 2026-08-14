package com.sandymandy.pleasurehorizons.client.gui.screen.hud;

import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.util.GirlStatusCache;
import com.sandymandy.pleasurehorizons.util.inventory.GirlInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.sandymandy.pleasurehorizons.util.PleasureHorizonsIcons.HEART_ICON;

/**
 * Compact status panel for the nearest owned girl: what she is doing, her health, her backpack
 * fill and the owner's hunger. Purely client-side; only the backpack fill needs server data, and
 * that arrives via {@code GirlStatusS2CPacket}.
 */
@OnlyIn(Dist.CLIENT)
public class GirlStatusOverlay {

    private static final double TRACK_RANGE = 24.0D;

    private GirlStatusOverlay() {
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        LocalPlayer player = mc.player;

        // The scene progress bar already occupies the left edge; avoid stacking panels.
        if (player.getVehicle() instanceof GirlSceneEntity) return;

        TameableGirlEntity girl = nearestOwnedGirl(player);
        if (girl == null) return;

        int x = 8;
        int y = 8;
        int lineHeight = 11;
        int panelWidth = 0;

        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(girl.getGirlDisplayName()));
        lines.add(Component.translatable("hud.pleasurehorizons.role",
                Component.translatable("role.pleasurehorizons." + girl.getRole().id())));
        lines.add(buildActivity(girl));
        lines.add(Component.literal(
                Math.round(girl.getHealth()) + "/" + Math.round(girl.getMaxHealth())));

        int used = GirlStatusCache.backpackUsed(girl.getId());
        lines.add(Component.translatable("hud.pleasurehorizons.backpack",
                used < 0 ? "?" : String.valueOf(used), GirlInventory.BACKPACK_END - GirlInventory.BACKPACK_START + 1));

        lines.add(Component.translatable("hud.pleasurehorizons.hunger",
                player.getFoodData().getFoodLevel(), 20));

        for (Component line : lines) {
            panelWidth = Math.max(panelWidth, mc.font.width(line));
        }
        panelWidth += 22; // heart icon + padding

        guiGraphics.fill(x, y, x + panelWidth, y + lines.size() * lineHeight + 6, 0x99000000);
        guiGraphics.blit(HEART_ICON, x + 2, y + 3 * lineHeight, 0, 0, 9, 9, 9, 9);

        int ty = y + 2;
        for (int i = 0; i < lines.size(); i++) {
            guiGraphics.drawString(mc.font, lines.get(i), x + 4, ty, 0xFFFFFFFF, true);
            ty += lineHeight;
        }
    }

    private static TameableGirlEntity nearestOwnedGirl(LocalPlayer player) {
        AABB area = player.getBoundingBox().inflate(TRACK_RANGE, 12.0D, TRACK_RANGE);
        List<TameableGirlEntity> girls = player.level().getEntitiesOfClass(
                TameableGirlEntity.class, area,
                girl -> girl.isTamed() && girl.isOwner(player) && girl.isAlive());
        return girls.stream()
                .min(Comparator.comparingDouble(girl -> girl.distanceToSqr(player)))
                .orElse(null);
    }

    /** Builds a short human-readable summary of what the girl is currently doing. */
    private static Component buildActivity(TameableGirlEntity girl) {
        List<String> parts = new ArrayList<>();
        if (girl.isSceneActive()) parts.add(t("hud.pleasurehorizons.activity.scene"));
        if (girl.isPassenger()) parts.add(t("hud.pleasurehorizons.activity.carried"));
        if (girl.isDowned()) parts.add(t("hud.pleasurehorizons.activity.downed"));
        if (girl.isSitting()) parts.add(t("hud.pleasurehorizons.activity.sitting"));
        if (girl.isFollowing()) parts.add(t("hud.pleasurehorizons.activity.following"));
        if (girl.isGuardBaseEnabled()) parts.add(t("gui.pleasurehorizons.button.guardBase"));
        if (girl.isGuardOwnerEnabled()) parts.add(t("gui.pleasurehorizons.button.guardOwner"));
        if (girl.isStayNearBaseEnabled()) parts.add(t("gui.pleasurehorizons.button.stayNearBase"));
        if (girl.isGatherEnabled()) parts.add(t("gui.pleasurehorizons.button.gather"));
        if (girl.isHarvestEnabled()) parts.add(t("gui.pleasurehorizons.button.harvest"));
        if (girl.isChopTreesEnabled()) parts.add(t("gui.pleasurehorizons.button.chopTrees"));
        if (girl.isHuntEnabled()) parts.add(t("gui.pleasurehorizons.button.hunt"));
        if (girl.isCookEnabled()) parts.add(t("gui.pleasurehorizons.button.cook"));
        if (girl.isFeedOwnerEnabled()) parts.add(t("gui.pleasurehorizons.button.feedOwner"));
        if (parts.isEmpty()) parts.add(t("hud.pleasurehorizons.activity.idle"));

        return Component.literal(String.join(" \u00b7 ", parts));
    }

    private static String t(String key) {
        return Component.translatable(key).getString();
    }
}
