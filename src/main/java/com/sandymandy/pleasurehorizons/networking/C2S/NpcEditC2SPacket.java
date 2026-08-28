package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.item.items.GirlWandItem;
import com.sandymandy.pleasurehorizons.relationship.AffectionData;
import com.sandymandy.pleasurehorizons.relationship.DialogueDB;
import com.sandymandy.pleasurehorizons.relationship.QuestManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-authoritative actions from the Girl Wand NPC editor.
 *
 * <ul>
 *   <li>{@code ADD_AFFECTION} — +10 affinity (short anti-spam cooldown).</li>
 *   <li>{@code RENAME} — set the girl's custom name.</li>
 *   <li>{@code GO_HOME} — teleport her to her saved base position.</li>
 *   <li>{@code TALK} — show her affinity-tier greeting.</li>
 *   <li>{@code QUEST_ACCEPT} / {@code QUEST_COMPLETE} — quest lifecycle.</li>
 * </ul>
 */
public record NpcEditC2SPacket(int entityId, Action action, String stringValue) implements CustomPacketPayload {

    public enum Action { ADD_AFFECTION, RENAME, GO_HOME, TALK, QUEST_ACCEPT, QUEST_COMPLETE }

    public static final Type<NpcEditC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "npceditc2spacket"));

    private static final StreamCodec<RegistryFriendlyByteBuf, Action> ACTION_CODEC = new StreamCodec<>() {
        @Override
        public Action decode(RegistryFriendlyByteBuf buf) {
            String name = ByteBufCodecs.STRING_UTF8.decode(buf);
            try {
                return Action.valueOf(name);
            } catch (IllegalArgumentException e) {
                return Action.ADD_AFFECTION;
            }
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, Action value) {
            ByteBufCodecs.STRING_UTF8.encode(buf, value.name());
        }
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, NpcEditC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, NpcEditC2SPacket::entityId,
                    ACTION_CODEC, NpcEditC2SPacket::action,
                    ByteBufCodecs.STRING_UTF8, NpcEditC2SPacket::stringValue,
                    NpcEditC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.player() instanceof ServerPlayer sp ? sp : null;
            if (player == null) return;
            if (!holdsWand(player)) return;

            Entity entity = player.level().getEntity(this.entityId());
            if (!(entity instanceof GirlEntity girl)) return;
            java.util.UUID owner = ownerOf(girl);
            if (owner != null && !owner.equals(player.getUUID())) {
                player.displayClientMessage(Component.translatable("msg.pleasurehorizons.wand_not_owner", girlName(girl)), true);
                return;
            }

            switch (this.action()) {
                case ADD_AFFECTION -> addAffection(player, girl);
                case RENAME -> rename(player, girl, this.stringValue());
                case GO_HOME -> goHome(player, girl);
                case TALK -> talk(player, girl);
                case QUEST_ACCEPT -> acceptQuest(player, girl);
                case QUEST_COMPLETE -> completeQuest(player, girl);
            }
        });
    }

    private static boolean holdsWand(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        return main.getItem() instanceof GirlWandItem || off.getItem() instanceof GirlWandItem;
    }

    private static java.util.UUID ownerOf(GirlEntity girl) {
        return girl instanceof TameableGirlEntity tamed ? tamed.getOwnerUUID() : null;
    }

    private static String girlName(GirlEntity girl) {
        if (girl.hasCustomName()) {
            return girl.getCustomName().getString();
        }
        String id = girl.getGirlID();
        return id.isEmpty() ? "Girl" : Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }

    private static void addAffection(ServerPlayer player, GirlEntity girl) {
        long now = player.level().getGameTime();
        if (now - LAST_AFFECTION_TICK.getOrDefault(player.getUUID(), -100L) < 20L) {
            player.displayClientMessage(Component.translatable("msg.pleasurehorizons.editor_affection_too_fast"), true);
            return;
        }
        LAST_AFFECTION_TICK.put(player.getUUID(), now);
        girl.addAffection(10);
        player.displayClientMessage(Component.translatable(
                "msg.pleasurehorizons.editor_affection", girlName(girl), girl.getAffection()), true);
    }

    private static void rename(ServerPlayer player, GirlEntity girl, String value) {
        String name = value.replace("§", "").trim();
        if (name.isEmpty() || name.length() > 32) {
            player.displayClientMessage(Component.translatable("msg.pleasurehorizons.editor_name_invalid"), true);
            return;
        }
        girl.setCustomName(Component.literal(name));
        girl.setCustomNameVisible(true);
        player.displayClientMessage(Component.translatable("msg.pleasurehorizons.editor_renamed", name), true);
    }

    private static void goHome(ServerPlayer player, GirlEntity girl) {
        BlockPos home = girl.getBasePos();
        if (home == null || home.equals(BlockPos.ZERO)) {
            player.displayClientMessage(Component.translatable(
                    "msg.pleasurehorizons.editor_no_home", girlName(girl)), true);
            return;
        }
        BlockPos.MutableBlockPos landing = home.mutable();
        while (landing.getY() < player.level().getMaxBuildHeight() - 1
                && !(player.level().getBlockState(landing).isAir()
                        && player.level().getBlockState(landing.above()).isAir())) {
            landing.move(0, 1, 0);
        }
        girl.teleportTo(landing.getX() + 0.5D, landing.getY(), landing.getZ() + 0.5D);
        player.displayClientMessage(Component.translatable(
                "msg.pleasurehorizons.editor_sent_home", girlName(girl)), true);
    }

    private static void talk(ServerPlayer player, GirlEntity girl) {
        AffectionData.AffectionLevel level = girl.getAffectionData().getLevel();
        String key = DialogueDB.greetingKey(girl.getGirlID(), level);
        player.displayClientMessage(Component.translatable(
                "chat.pleasurehorizons.girlSays", girlName(girl), Component.translatable(key)), false);
        player.displayClientMessage(Component.translatable(
                "msg.pleasurehorizons.editor_talk", girlName(girl),
                Component.translatable(level.labelKey)), true);
    }

    private static void acceptQuest(ServerPlayer player, GirlEntity girl) {
        QuestManager manager = girl.getQuestManager();
        if (!manager.canEdit(player, girl)) {
            player.displayClientMessage(Component.translatable("msg.pleasurehorizons.editor_not_yours"), true);
            return;
        }
        if (manager.hasActiveQuest()) {
            QuestManager.Quest active = manager.activeQuest();
            player.displayClientMessage(Component.translatable(
                    "msg.pleasurehorizons.quest_active", Component.translatable(active.descriptionKey()),
                    manager.getProgress(), manager.getTarget()), true);
            return;
        }
        QuestManager.Quest quest = manager.availableQuest(girl.getGirlID());
        if (quest == null) {
            player.displayClientMessage(Component.translatable(
                    "msg.pleasurehorizons.quest_done_all", girlName(girl)), true);
            return;
        }
        manager.startQuest(quest, player.getUUID());
        player.displayClientMessage(Component.translatable(
                "msg.pleasurehorizons.quest_started", girlName(girl), Component.translatable(quest.descriptionKey())), true);
    }

    private static void completeQuest(ServerPlayer player, GirlEntity girl) {
        QuestManager manager = girl.getQuestManager();
        QuestManager.Quest quest = manager.activeQuest();
        if (quest == null) {
            player.displayClientMessage(Component.translatable("msg.pleasurehorizons.quest_none"), true);
            return;
        }
        boolean ready = switch (quest.type()) {
            case FETCH -> manager.tryCollectFetch(player);
            case KILL -> manager.getProgress() >= manager.getTarget();
            case ESCORT -> manager.girlMeetsEscort(girl, quest);
            case DEFEND -> manager.getProgress() >= manager.getTarget();
        };
        if (!ready) {
            player.displayClientMessage(Component.translatable(
                    "msg.pleasurehorizons.quest_not_ready", Component.translatable(quest.descriptionKey()),
                    manager.getProgress(), manager.getTarget()), true);
            return;
        }
        QuestManager.Quest finished = manager.complete();
        manager.grantReward(girl, player, finished);
        player.displayClientMessage(Component.translatable(
                "msg.pleasurehorizons.quest_completed", girlName(girl),
                finished.rewardAffection()), true);
    }

    private static final Map<UUID, Long> LAST_AFFECTION_TICK = new HashMap<>();
}
