package io.github.lexikiq.crowdcontrol.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import io.github.lexikiq.crowdcontrol.utils.BlockUtil;
import io.github.lexikiq.crowdcontrol.utils.RandomUtil;
import io.github.lexikiq.crowdcontrol.utils.WeightedEnum;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LootboxCommand extends ChatCommand {
    public LootboxCommand(CrowdControl plugin) {
        super(plugin);
    }

    public enum EnchantmentWeights implements WeightedEnum {
        ONE(1, 40),
        TWO(2, 15),
        THREE(3, 3),
        FOUR(4, 2),
        FIVE(5, 1)
        ;

        private final @Getter int level;
        private final @Getter int weight;
        EnchantmentWeights(int level, int weight){
            this.level = level;
            this.weight = weight;
        }

        public static final int TOTAL_WEIGHTS = Arrays.stream(values()).mapToInt(EnchantmentWeights::getWeight).sum();
    }

    public enum AttributeWeights implements WeightedEnum {
        NONE(0, 167),
        ONE(1, 20),
        TWO(2, 10),
        THREE(3, 2),
        FOUR(4, 1)
        ;

        private final @Getter int level;
        private final @Getter int weight;
        AttributeWeights(int level, int weight){
            this.level = level;
            this.weight = weight;
        }

        public static final int TOTAL_WEIGHTS = Arrays.stream(values()).mapToInt(AttributeWeights::getWeight).sum();
    }

    @Override
    public int getCooldownSeconds() {
        return 30;
    }

    @Override
    public @NotNull String getCommand() {
        return "lootbox";
    }

    @Override
    public boolean execute(ChannelMessageEvent event, List<Player> players, String... args) {
        for (Player player : players) {
            Inventory lootbox = Bukkit.createInventory(null, 27, event.getUser().getName()+" has gifted you...");
            List<Material> items = new ArrayList<>(BlockUtil.MATERIAL_SET);
            Collections.shuffle(items, rand);
            Material item = null;
            for (Material i : items) {
                if (i.isItem()) {
                    item = i;
                    break;
                }
            }
            assert item != null;
            ItemStack itemStack = new ItemStack(item, 1+rand.nextInt(item.getMaxStackSize()));
            // big dumb enchantment logic to generate sane items lmfao
            int enchantments = ((EnchantmentWeights) RandomUtil.weightedRandom(EnchantmentWeights.values(), EnchantmentWeights.TOTAL_WEIGHTS)).getLevel();
            if (enchantments > 0) {
                List<Enchantment> enchantmentList = new ArrayList<>();
                for (Enchantment enchantment : EnchantmentWrapper.values()) {
                    if(enchantment.canEnchantItem(itemStack)) enchantmentList.add(enchantment);
                }
                if (!enchantmentList.isEmpty()) {
                    Collections.shuffle(enchantmentList, rand);
                    Set<Enchantment> addedEnchantments = new HashSet<>();
                    int count = 0;
                    for (int i = 0; i < enchantmentList.size() && count < enchantments; ++i) {
                        Enchantment enchantment = enchantmentList.get(i);
                        if (addedEnchantments.stream().noneMatch(x -> x.conflictsWith(enchantment))) {
                            ++count;
                            addedEnchantments.add(enchantment);
                            int level = enchantment.getStartLevel()+rand.nextInt(enchantment.getMaxLevel()-enchantment.getStartLevel()+1);
                            itemStack.addEnchantment(enchantment, level);
                        }
                    }
                }
            }
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setLore(List.of("Donated by "+event.getUser().getName()));
            if (rand.nextDouble() >= 0.9D) {
                itemMeta.setUnbreakable(true);
            }

            int attributes = ((AttributeWeights) RandomUtil.weightedRandom(AttributeWeights.values(), AttributeWeights.TOTAL_WEIGHTS)).getLevel();
            if (attributes > 0) {
                List<Attribute> attributeList = Arrays.asList(Attribute.values());
                Collections.shuffle(attributeList, rand);
                for (int i = 0; i < attributeList.size() && i < attributes; ++i) {
                    Attribute attribute = attributeList.get(i);
                    String name = "lootbox_" + attribute.getKey().getKey();
                    AttributeModifier attributeModifier = new AttributeModifier(name, (rand.nextDouble()*2)-1, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
                    itemMeta.addAttributeModifier(attribute, attributeModifier);
                }
            }

            itemStack.setItemMeta(itemMeta);
            lootbox.setItem(13, itemStack);
            new BukkitRunnable(){
                @Override
                public void run() {
                    player.openInventory(lootbox);
                }
            }.runTask(plugin);
        }
        return true;
    }
}
