package net.Indyuce.mmoitems.gui.edition;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.gui.Navigator;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.particle.api.ParticleType;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ParticlesEdition extends EditionInventory {
    private static final NamespacedKey PATTERN_MODIFIED_KEY = new NamespacedKey(MMOItems.plugin, "PatternModifierId");

    public ParticlesEdition(Navigator navigator, MMOItemTemplate template) {
        super(navigator, template);
    }

    @Override
    public String getName() {
        return "粒子效果: " + template.getId();
    }

    @Override
    public void arrangeInventory() {
        int[] slots = {37, 38, 39, 40, 41, 42, 43};
        int n = 0;

        @Nullable ParticleType particleType = null;
        try {
            particleType = ParticleType.valueOf(getEditedSection().getString("item-particles.type"));
        } catch (Exception ignored) {
        }

        ItemStack particleTypeItem = new ItemStack(Material.PINK_STAINED_GLASS);
        ItemMeta particleTypeItemMeta = particleTypeItem.getItemMeta();
        particleTypeItemMeta.setDisplayName(ChatColor.GREEN + "粒子模式");
        List<String> particleTypeItemLore = new ArrayList<>();
        particleTypeItemLore.add(ChatColor.GRAY + "粒子模式定义粒子的行为方式");
        particleTypeItemLore.add(ChatColor.GRAY + "显示时遵循的模式或");
        particleTypeItemLore.add(ChatColor.GRAY + "形成的形状.");
        particleTypeItemLore.add("");
        particleTypeItemLore.add(ChatColor.GRAY + "当前值: "
                + (particleType == null ? ChatColor.RED + "未选择类型." : ChatColor.GOLD + particleType.getDefaultName()));
        if (particleType != null) {
            particleTypeItemLore.add("" + ChatColor.GRAY + ChatColor.ITALIC + particleType.getDescription());
        }
        particleTypeItemLore.add("");
        particleTypeItemLore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击进行选择");
        particleTypeItemLore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击更改此值");
        particleTypeItemMeta.setLore(particleTypeItemLore);
        particleTypeItem.setItemMeta(particleTypeItemMeta);

        if (particleType != null) {
            ConfigurationSection psection = getEditedSection().getConfigurationSection("item-particles");
            for (String modifier : particleType.getModifiers()) {
                final ItemStack modifierItem = new ItemStack(Material.GRAY_DYE);
                ItemMeta modifierItemMeta = modifierItem.getItemMeta();
                modifierItemMeta.setDisplayName(ChatColor.GREEN + UtilityMethods.caseOnWords(modifier.toLowerCase().replace("-", " ")));
                List<String> modifierItemLore = new ArrayList<>();
                modifierItemLore.add("" + ChatColor.GRAY + ChatColor.ITALIC + "这是一个模式编辑器.");
                modifierItemLore.add("" + ChatColor.GRAY + ChatColor.ITALIC + "更改该值可略微");
                modifierItemLore.add("" + ChatColor.GRAY + ChatColor.ITALIC + "调整粒子模式.");
                modifierItemLore.add("");
                modifierItemLore.add(ChatColor.GRAY + "当前值: " + ChatColor.GOLD
                        + (psection.contains(modifier) ? psection.getDouble(modifier) : particleType.getModifier(modifier)));
                modifierItemMeta.setLore(modifierItemLore);
                modifierItemMeta.getPersistentDataContainer().set(PATTERN_MODIFIED_KEY, PersistentDataType.STRING, modifier);
                modifierItem.setItemMeta(modifierItemMeta);

                inventory.setItem(slots[n++], modifierItem);
            }
        }

        @Nullable Particle particle = null;
        try {
            particle = Particle.valueOf(getEditedSection().getString("item-particles.particle"));
        } catch (Exception ignored) {
        }

        ItemStack particleItem = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta particleItemMeta = particleItem.getItemMeta();
        particleItemMeta.setDisplayName(ChatColor.GREEN + "粒子");
        List<String> particleItemLore = new ArrayList<>();
        particleItemLore.add(ChatColor.GRAY + "定义粒子效果中");
        particleItemLore.add(ChatColor.GRAY + "使用的粒子");
        particleItemLore.add("");
        particleItemLore.add(ChatColor.GRAY + "当前值: " + (particle == null ? ChatColor.RED + "未选择任何粒子"
                : ChatColor.GOLD + UtilityMethods.caseOnWords(particle.name().toLowerCase().replace("_", " "))));
        particleItemLore.add("");
        particleItemLore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击进行选择");
        particleItemLore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击更改此值");
        particleItemMeta.setLore(particleItemLore);
        particleItem.setItemMeta(particleItemMeta);

        if (particle != null && MMOUtils.isColorable(particle)) {
            int red = getEditedSection().getInt("item-particles.color.red");
            int green = getEditedSection().getInt("item-particles.color.green");
            int blue = getEditedSection().getInt("item-particles.color.blue");

            ItemStack colorItem = new ItemStack(Material.RED_DYE);
            ItemMeta colorItemMeta = colorItem.getItemMeta();
            colorItemMeta.setDisplayName(ChatColor.GREEN + "粒子颜色");
            List<String> colorItemLore = new ArrayList<>();
            colorItemLore.add(ChatColor.GRAY + "粒子的 RGB 颜色");
            colorItemLore.add("");
            colorItemLore.add(ChatColor.GRAY + "当前值 (R-G-B):");
            colorItemLore.add("" + ChatColor.RED + ChatColor.BOLD + red + ChatColor.GRAY + " - " + ChatColor.GREEN + ChatColor.BOLD + green
                    + ChatColor.GRAY + " - " + ChatColor.BLUE + ChatColor.BOLD + blue);
            colorItemLore.add("");
            colorItemLore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击进行选择");
            colorItemLore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击更改此值");
            colorItemMeta.setLore(colorItemLore);
            colorItem.setItemMeta(colorItemMeta);

            inventory.setItem(25, colorItem);
        }

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(ChatColor.RED + "- 未编辑 -");
        glass.setItemMeta(glassMeta);

        while (n < slots.length)
            inventory.setItem(slots[n++], glass);

        inventory.setItem(21, particleTypeItem);
        inventory.setItem(23, particleItem);
    }

    @Override
    public void whenClicked(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();

        event.setCancelled(true);
        if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isMetaItem(item, false))
            return;

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "粒子")) {
            if (event.getAction() == InventoryAction.PICKUP_ALL)
                new StatEdition(this, ItemStats.ITEM_PARTICLES, "particle").enable("在聊天栏中输入您想要的粒子");

            if (event.getAction() == InventoryAction.PICKUP_HALF) {
                if (getEditedSection().contains("item-particles.particle")) {
                    getEditedSection().set("item-particles.particle", null);
                    registerTemplateEdition();
                    player.sendMessage(MMOItems.plugin.getPrefix() + "成功重置粒子");
                }
            }
        }

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "粒子颜色")) {
            if (event.getAction() == InventoryAction.PICKUP_ALL)
                new StatEdition(this, ItemStats.ITEM_PARTICLES, "particle-color").enable("在聊天栏中输入您想要的RGB颜色",
                        ChatColor.AQUA + "Format: [RED] [GREEN] [BLUE]");

            if (event.getAction() == InventoryAction.PICKUP_HALF) {
                if (getEditedSection().contains("item-particles.color")) {
                    getEditedSection().set("item-particles.color", null);
                    registerTemplateEdition();
                    player.sendMessage(MMOItems.plugin.getPrefix() + "成功重置粒子颜色");
                }
            }
        }

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "粒子模式")) {
            if (event.getAction() == InventoryAction.PICKUP_ALL) {
                new StatEdition(this, ItemStats.ITEM_PARTICLES, "particle-type").enable("在聊天栏中输入您想要的粒子模式");
                player.sendMessage("");
                player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "可用的粒子模式");
                for (ParticleType type : ParticleType.values())
                    player.sendMessage("* " + ChatColor.GREEN + type.name());
            }

            if (event.getAction() == InventoryAction.PICKUP_HALF) {
                if (getEditedSection().contains("item-particles.type")) {
                    getEditedSection().set("item-particles.type", null);

                    // reset other modifiers
                    for (String key : getEditedSection().getConfigurationSection("item-particles").getKeys(false))
                        if (!key.equals("particle"))
                            getEditedSection().set("item-particles." + key, null);

                    registerTemplateEdition();
                    player.sendMessage(MMOItems.plugin.getPrefix() + "成功重置粒子模式.");
                }
            }
        }

        final String tag = item.getItemMeta().getPersistentDataContainer().get(PATTERN_MODIFIED_KEY, PersistentDataType.STRING);
        if (tag == null || tag.equals("")) return;

        if (event.getAction() == InventoryAction.PICKUP_ALL)
            new StatEdition(this, ItemStats.ITEM_PARTICLES, tag).enable("在聊天栏中输入您想要的值");

        if (event.getAction() == InventoryAction.PICKUP_HALF) {
            if (getEditedSection().contains("item-particles." + tag)) {
                getEditedSection().set("item-particles." + tag, null);
                registerTemplateEdition();
                player.sendMessage(MMOItems.plugin.getPrefix() + "重置成功 " + ChatColor.GOLD + tag + ChatColor.GRAY + ".");
            }
        }
    }
}