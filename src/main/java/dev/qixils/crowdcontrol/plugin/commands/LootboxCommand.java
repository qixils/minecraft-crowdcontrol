package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ChatCommand;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.BlockUtil;
import dev.qixils.crowdcontrol.plugin.utils.RandomUtil;
import dev.qixils.crowdcontrol.plugin.utils.TextBuilder;
import dev.qixils.crowdcontrol.plugin.utils.Weighted;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.text.format.TextDecoration;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class LootboxCommand extends ChatCommand {
    public LootboxCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    private final String effectName = "lootbox";
    private final String displayName = "Open Lootbox";

    public enum EnchantmentWeights implements Weighted {
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

    public enum AttributeWeights implements Weighted {
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
    public Response.Result execute(Request request) {
        for (Player player : CrowdControlPlugin.getPlayers()) {
            Inventory lootbox = Bukkit.createInventory(null, 27, new TextBuilder(request.getViewer(), CrowdControlPlugin.USER_COLOR).next(" has gifted you...").build());
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
            int enchantments = RandomUtil.weightedRandom(EnchantmentWeights.values(), EnchantmentWeights.TOTAL_WEIGHTS).getLevel();
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
            itemMeta.lore(Collections.singletonList(new TextBuilder("Donated by ").next(request.getViewer(), CrowdControlPlugin.USER_COLOR, TextDecoration.ITALIC).build()));
            if (rand.nextDouble() >= 0.9D) {
                itemMeta.setUnbreakable(true);
            }

            int attributes = RandomUtil.weightedRandom(AttributeWeights.values(), AttributeWeights.TOTAL_WEIGHTS).getLevel();
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
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(lootbox));
        }
        return Response.Result.SUCCESS;
    }
}
