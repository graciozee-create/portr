package com.sandymandy.pleasurehorizons.relationship;

import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Lightweight per-character quest system.
 *
 * <p>Supported quest types:</p>
 * <ul>
 *   <li>{@code FETCH} — collect items and press "Complete" while holding them.</li>
 *   <li>{@code KILL} — kills of a specific mob are counted server-side and auto-complete.</li>
 *   <li>{@code ESCORT} — bring the girl to a biome/height and press "Complete".</li>
 * </ul>
 *
 * <p>Quest state is stored per girl entity (NBT) and girls are protected by the same ownership
 * rules as the NPC editor. Quests are deliberately defined only for girl ids that exist in this
 * mod; unknown/custom girls fall back to the generic profile set.</p>
 */
public class QuestManager {

    public enum QuestType { FETCH, KILL, ESCORT, DEFEND }

    /** A quest definition. {@code itemId}/{@code rewardItemId}/{@code mobId} are registry ids. */
    public record Quest(String id, QuestType type, String itemId, int count,
                        int rewardAffection, String rewardItemId, String girlId,
                        String mobId, String biomeTag, int maxY, int defendWaves) {

        public String descriptionKey() {
            return "quest.pleasurehorizons." + id + ".desc";
        }

        public String itemName() {
            return "item.pleasurehorizons.quest." + id + ".item";
        }
    }

    private static final String TAG_ACTIVE = "ActiveQuest";
    private static final String TAG_PROGRESS = "QuestProgress";
    private static final String TAG_COMPLETED = "CompletedQuests";
    private static final String TAG_OWNER = "QuestOwner";

    private String activeQuestId = "";
    private int questProgress = 0;
    private final Set<String> completedQuests = new HashSet<>();
    private String questOwner = "";

    private static final Map<String, List<Quest>> QUESTS = new LinkedHashMap<>();

    // ------------------------------------------------------------------ registry

    static {
        // LUCY — cheerful gatherer
        register("lucy", new Quest("lucy_flowers", QuestType.FETCH, "minecraft:poppy", 8, 15, "pleasurehorizons:gift_red_rose", "lucy", "", "", 0, 0));
        register("lucy", new Quest("lucy_sugar", QuestType.FETCH, "minecraft:sugar", 6, 15, "", "lucy", "", "", 0, 0));
        register("lucy", new Quest("lucy_kill_zombie", QuestType.KILL, "", 4, 20, "pleasurehorizons:healing_charm", "lucy", "minecraft:zombie", "", 0, 0));

        // MIKA — quiet explorer
        register("mika", new Quest("mika_lapis", QuestType.FETCH, "minecraft:lapis_lazuli", 8, 20, "", "mika", "", "", 0, 0));
        register("mika", new Quest("mika_kill_skeleton", QuestType.KILL, "", 4, 20, "pleasurehorizons:gift_enchanted_quill", "mika", "minecraft:skeleton", "", 0, 0));

        // MOMO — playful cook
        register("momo", new Quest("momo_honey", QuestType.FETCH, "minecraft:honeycomb", 6, 15, "pleasurehorizons:gift_golden_honeycomb", "momo", "", "", 0, 0));
        register("momo", new Quest("momo_kill_spider", QuestType.KILL, "", 3, 18, "", "momo", "minecraft:spider", "", 0, 0));

        // SLIME — curious substance
        register("slime", new Quest("slime_balls", QuestType.FETCH, "minecraft:slime_ball", 6, 20, "pleasurehorizons:gift_crystal_slime", "slime", "", "", 0, 0));
        register("slime", new Quest("slime_kill_zombie", QuestType.KILL, "", 5, 20, "", "slime", "minecraft:zombie", "", 0, 0));

        // KOBOLD — greedy collector
        register("kobold", new Quest("kobold_nuggets", QuestType.FETCH, "minecraft:gold_nugget", 12, 15, "", "kobold", "", "", 0, 0));
        register("kobold", new Quest("kobold_amethyst", QuestType.FETCH, "minecraft:amethyst_shard", 4, 20, "pleasurehorizons:gift_diamond_ring", "kobold", "", "", 0, 0));
        register("kobold", new Quest("kobold_kill_zombie", QuestType.KILL, "", 5, 20, "", "kobold", "minecraft:zombie", "", 0, 0));

        // COPPIE — cat-like fish lover
        register("coppie", new Quest("coppie_salmon", QuestType.FETCH, "minecraft:salmon", 5, 15, "pleasurehorizons:gift_silver_bell", "coppie", "", "", 0, 0));
        register("coppie", new Quest("coppie_escort_beach", QuestType.ESCORT, "", 1, 25, "pleasurehorizons:gift_silver_bell", "coppie", "", "minecraft:is_beach", 0, 0));

        // ALLIE — gentle healer
        register("allie", new Quest("allie_poppies", QuestType.FETCH, "minecraft:poppy", 10, 15, "pleasurehorizons:gift_moonlight_lily", "allie", "", "", 0, 0));
        register("allie", new Quest("allie_apple", QuestType.FETCH, "minecraft:golden_apple", 1, 25, "pleasurehorizons:healing_charm", "allie", "", "", 0, 0));
        register("allie", new Quest("allie_kill_spider", QuestType.KILL, "", 4, 22, "", "allie", "minecraft:spider", "", 0, 0));
        register("allie", new Quest("allie_escort_forest", QuestType.ESCORT, "", 1, 25, "", "allie", "", "minecraft:is_forest", 0, 0));

        // BIA — mysterious collector
        register("bia", new Quest("bia_amethyst", QuestType.FETCH, "minecraft:amethyst_shard", 5, 20, "", "bia", "", "", 0, 0));
        register("bia", new Quest("bia_echo", QuestType.FETCH, "minecraft:echo_shard", 2, 25, "pleasurehorizons:gift_ancient_coin", "bia", "", "", 0, 0));
        register("bia", new Quest("bia_kill_enderman", QuestType.KILL, "", 2, 30, "", "bia", "minecraft:enderman", "", 0, 0));

        // GOBLIN — shiny hoarder
        register("goblin", new Quest("goblin_gold", QuestType.FETCH, "minecraft:gold_ingot", 4, 25, "pleasurehorizons:gift_ancient_coin", "goblin", "", "", 0, 0));
        register("goblin", new Quest("goblin_emerald", QuestType.FETCH, "minecraft:emerald", 3, 25, "", "goblin", "", "", 0, 0));
        register("goblin", new Quest("goblin_escort_cave", QuestType.ESCORT, "", 1, 25, "pleasurehorizons:gift_diamond_ring", "goblin", "", "", 32, 0));

        // GALATH — void-touched traveler
        register("galath", new Quest("galath_pearls", QuestType.FETCH, "minecraft:ender_pearl", 3, 25, "", "galath", "", "", 0, 0));
        register("galath", new Quest("galath_obsidian", QuestType.FETCH, "minecraft:obsidian", 5, 20, "", "galath", "", "", 0, 0));
        register("galath", new Quest("galath_kill_wither", QuestType.KILL, "", 3, 30, "pleasurehorizons:galath_coin", "galath", "minecraft:wither_skeleton", "", 0, 0));

        // MANGLELIE — woodland guardian
        register("manglelie", new Quest("manglelie_rose", QuestType.FETCH, "pleasurehorizons:gift_red_rose", 1, 20, "", "manglelie", "", "", 0, 0));
        register("manglelie", new Quest("manglelie_kill_zombie", QuestType.KILL, "", 4, 20, "", "manglelie", "minecraft:zombie", "", 0, 0));

        // JENNY — gamer spirit
        register("jenny", new Quest("jenny_redstone", QuestType.FETCH, "minecraft:redstone", 12, 25, "pleasurehorizons:gift_enchanted_quill", "jenny", "", "", 0, 0));
        register("jenny", new Quest("jenny_comparator", QuestType.FETCH, "minecraft:comparator", 1, 15, "", "jenny", "", "", 0, 0));
        register("jenny", new Quest("jenny_kill_skeleton", QuestType.KILL, "", 4, 22, "", "jenny", "minecraft:skeleton", "", 0, 0));

        // CUSTOM / UNKNOWN — generic profile
        register("custom_girl", new Quest("custom_flowers", QuestType.FETCH, "minecraft:poppy", 8, 15, "pleasurehorizons:gift_red_rose", "custom_girl", "", "", 0, 0));
        register("custom_girl", new Quest("custom_gold", QuestType.FETCH, "minecraft:gold_ingot", 4, 20, "", "custom_girl", "", "", 0, 0));
        register("custom_girl", new Quest("custom_kill_zombie", QuestType.KILL, "", 4, 20, "pleasurehorizons:healing_charm", "custom_girl", "minecraft:zombie", "", 0, 0));
    }

    private static void register(String girlId, Quest quest) {
        QUESTS.computeIfAbsent(girlId, k -> new ArrayList<>()).add(quest);
    }

    // ------------------------------------------------------------------ state

    public Quest activeQuest() {
        if (this.activeQuestId.isEmpty()) {
            return null;
        }
        for (List<Quest> list : QUESTS.values()) {
            for (Quest quest : list) {
                if (quest.id().equals(this.activeQuestId)) {
                    return quest;
                }
            }
        }
        return null;
    }

    public boolean hasActiveQuest() {
        return !this.activeQuestId.isEmpty();
    }

    public int getProgress() {
        return this.questProgress;
    }

    public int getTarget() {
        Quest quest = this.activeQuest();
        return quest == null ? 0 : quest.count();
    }

    public String getOwner() {
        return this.questOwner;
    }

    public Quest availableQuest(String girlId) {
        List<Quest> all = QUESTS.getOrDefault(girlId,
                QUESTS.getOrDefault("custom_girl", List.of()));
        List<Quest> available = all.stream()
                .filter(q -> !this.completedQuests.contains(q.id()))
                .toList();
        if (available.isEmpty()) {
            return null;
        }
        int index = java.util.concurrent.ThreadLocalRandom.current().nextInt(available.size());
        return available.get(index);
    }

    public Quest startQuest(Quest quest, UUID owner) {
        this.activeQuestId = quest.id();
        this.questProgress = 0;
        this.questOwner = owner == null ? "" : owner.toString();
        return quest;
    }

    /** Mark the active quest complete, add it to the completion list and clear the slot. */
    public Quest complete() {
        Quest quest = this.activeQuest();
        if (quest != null) {
            this.completedQuests.add(quest.id());
        }
        this.activeQuestId = "";
        this.questProgress = 0;
        return quest;
    }

    public boolean addKill(ResourceLocation mob, int amount) {
        Quest quest = this.activeQuest();
        if (quest == null || (quest.type() != QuestType.KILL && quest.type() != QuestType.DEFEND)
                || quest.mobId().isEmpty()) {
            return false;
        }
        ResourceLocation target = ResourceLocation.parse(quest.mobId());
        if (target == null || !target.equals(mob)) {
            return false;
        }
        this.questProgress += amount;
        return this.questProgress >= quest.count();
    }

    public void setProgress(int value) {
        this.questProgress = Math.max(0, value);
    }

    // ------------------------------------------------------------------ NBT

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_ACTIVE, this.activeQuestId);
        tag.putInt(TAG_PROGRESS, this.questProgress);
        tag.putString(TAG_OWNER, this.questOwner);
        ListTag completed = new ListTag();
        for (String id : this.completedQuests) {
            completed.add(StringTag.valueOf(id));
        }
        tag.put(TAG_COMPLETED, completed);
        return tag;
    }

    public void fromNBT(CompoundTag tag) {
        this.activeQuestId = tag.getString(TAG_ACTIVE);
        this.questProgress = tag.getInt(TAG_PROGRESS);
        this.questOwner = tag.getString(TAG_OWNER);
        this.completedQuests.clear();
        if (tag.contains(TAG_COMPLETED)) {
            ListTag completed = tag.getList(TAG_COMPLETED, net.minecraft.nbt.Tag.TAG_STRING);
            for (int i = 0; i < completed.size(); i++) {
                this.completedQuests.add(completed.getString(i));
            }
        }
    }

    // ------------------------------------------------------------------ player helpers

    /** True if {@code player} may interact with this quest (owner or unowned). */
    public boolean canEdit(ServerPlayer player, GirlEntity girl) {
        java.util.UUID owner = girl instanceof TameableGirlEntity tamed ? tamed.getOwnerUUID() : null;
        if (owner != null && !owner.equals(player.getUUID())) {
            return false;
        }
        return this.questOwner.isEmpty() || this.questOwner.equals(player.getStringUUID());
    }

    /** Try to complete a FETCH quest by consuming the requested items from the player. */
    public boolean tryCollectFetch(ServerPlayer player) {
        Quest quest = this.activeQuest();
        if (quest == null || quest.type() != QuestType.FETCH) {
            return false;
        }
        ResourceLocation itemId = ResourceLocation.parse(quest.itemId());
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item == null) {
            return false;
        }
        int found = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                found += stack.getCount();
            }
        }
        if (found < quest.count()) {
            return false;
        }
        int remaining = quest.count();
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
            }
        }
        return true;
    }

    /** True when the girl is in the escort destination for the active ESCORT quest. */
    public boolean girlMeetsEscort(GirlEntity girl, Quest quest) {
        var pos = girl.blockPosition();
        if (quest.maxY() > 0 && pos.getY() >= quest.maxY()) {
            return false;
        }
        if (quest.biomeTag() == null || quest.biomeTag().isEmpty()) {
            return true;
        }
        var holder = girl.level().getBiome(pos);
        return holder.is(TagKey.create(Registries.BIOME, ResourceLocation.parse(quest.biomeTag())));
    }

    /** Give the quest reward (affection + optional item) to the girl/player. */
    public void grantReward(GirlEntity girl, ServerPlayer player, Quest quest) {
        girl.addAffection(quest.rewardAffection());
        if (quest.rewardItemId() != null && !quest.rewardItemId().isEmpty()) {
            Item reward = BuiltInRegistries.ITEM.get(ResourceLocation.parse(quest.rewardItemId()));
            if (reward != null) {
                if (!player.getInventory().add(new ItemStack(reward, 1))) {
                    player.drop(new ItemStack(reward, 1), false);
                }
            }
        }
    }
}
