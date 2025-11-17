package net.avicus.compendium.settings.menu;

import net.avicus.compendium.locale.text.Localizable;
import net.avicus.compendium.locale.text.UnlocalizedText;
import net.avicus.compendium.menu.inventory.InventoryMenu;
import net.avicus.compendium.plugin.Messages;
import net.avicus.compendium.settings.PlayerSettings;
import net.avicus.compendium.settings.Setting;
import net.avicus.compendium.settings.SettingStore;
import net.avicus.compendium.settings.types.BooleanSettingType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SettingsMenu extends InventoryMenu {
    public SettingsMenu(final Player viewer) {
        super(
                viewer,
                Messages.SETTINGS_MENU_TITLE.with(ChatColor.AQUA).render(viewer)
                        .toLegacyText(),
                1
        );

        List<Setting> list = new ArrayList<>(PlayerSettings.settings());
        list.sort(Comparator.comparing(setting -> setting.getName().render(viewer).toPlainText()));
        SettingStore store = PlayerSettings.store();

        int index = 0;
        for (Setting setting : list) {
            this.add(new SettingsMenuItem(viewer, this, index) {
                @Override
                public void onClick(ClickType type) {
                    store.toggle(viewer.getUniqueId(), setting);

                    this.parent.update(false);
                }

                @Override
                public ItemStack getItemStack() {
                    Object currentRaw = store.get(viewer.getUniqueId(), setting);
                    String current = setting.getType().value(currentRaw).serialize();
                    Localizable currentText = new UnlocalizedText(current, ChatColor.WHITE);
                    Localizable desc = setting.getSummary();

                    // not my favorite approach, but it works - maps SettingType to material
                    ItemStack stack;
                    if (setting.getType() instanceof BooleanSettingType) {
                        stack = new ItemStack(Material.INK_SACK, 1);
                        boolean isOn = "on".equals(current);
                        stack.setDurability((short) (isOn ? 10 : 1)); // 10 = lime, 1 = rose red
                    } else {
                        stack = new ItemStack(Material.PAPER, 1);
                    }

                    stack.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

                    ItemMeta meta = stack.getItemMeta();
                    meta.setDisplayName(setting.getName().render(viewer).toLegacyText());
                    List<String> lore = new ArrayList<>();

                    // Wrap description text if longer than MAX_LENGTH
                    String descText = desc.render(viewer).toLegacyText();
                    if (descText.length() > MAX_LENGTH) {
                        List<String> wrappedLines = wrapText(descText, MAX_LENGTH);
                        lore.addAll(wrappedLines);
                    } else {
                        lore.add(descText);
                    }

                    lore.add(currentText.render(viewer).toLegacyText());
                    meta.setLore(lore);
                    stack.setItemMeta(meta);
                    return stack;
                }
            });
            index++;
        }
    }

    public static SettingsMenu create(final Player viewer) {
        return new SettingsMenu(viewer);
    }

    private static List<String> wrapText(String text, int maxLength) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxLength) {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                // If a single word is longer than maxLength, add it as is
                if (word.length() > maxLength) {
                    lines.add(word);
                } else {
                    currentLine.append(word);
                }
            } else {
                if (!currentLine.isEmpty()) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
}