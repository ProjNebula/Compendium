package net.avicus.compendium.settings.menu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.avicus.compendium.locale.text.Localizable;
import net.avicus.compendium.locale.text.UnlocalizedFormat;
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

/**
 * Inventory menu for displaying and modifying player settings.
 */
public class SettingsMenu extends InventoryMenu {

    private static final UnlocalizedFormat TWO_PART_FORMAT = new UnlocalizedFormat("{0}: {1}");
    private static final int MAX_LENGTH = 50;
    private static final short DYE_LIME_GREEN = 10;
    private static final short DYE_ROSE_RED = 1;
    private static final String SETTING_ON = "on";

    public SettingsMenu(final Player viewer) {
        super(
                viewer,
                Messages.SETTINGS_MENU_TITLE.with(ChatColor.AQUA).render(viewer).toLegacyText(),
                1
        );

        final List<Setting> settings = new ArrayList<>(PlayerSettings.settings());
        settings.sort(Comparator.comparing(setting -> setting.getName().render(viewer).toPlainText()));
        final SettingStore store = PlayerSettings.store();

        int index = 0;
        for (final Setting setting : settings) {
            this.add(createSettingMenuItem(viewer, store, setting, index));
            index++;
        }
    }

    public static SettingsMenu create(final Player viewer) {
        return new SettingsMenu(viewer);
    }

    private SettingsMenuItem createSettingMenuItem(
            final Player viewer,
            final SettingStore store,
            final Setting setting,
            final int index
    ) {
        return new SettingsMenuItem(viewer, this, index) {
            @Override
            public void onClick(final ClickType type) {
                store.toggle(viewer.getUniqueId(), setting);
                this.parent.update(false);
            }

            @Override
            public ItemStack getItemStack() {
                final Object currentRaw = store.get(viewer.getUniqueId(), setting);
                final String current = setting.getType().value(currentRaw).serialize();
                final Localizable currentText = new UnlocalizedText(current, ChatColor.WHITE);
                final Localizable description = setting.getSummary();

                final ItemStack stack = createItemStackForSetting(setting, current);
                stack.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

                final ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(setting.getName().render(viewer).toLegacyText());
                meta.setLore(createLore(description, currentText, viewer));
                stack.setItemMeta(meta);

                return stack;
            }
        };
    }

    private ItemStack createItemStackForSetting(final Setting setting, final String currentValue) {
        if (setting.getType() instanceof BooleanSettingType) {
            final ItemStack stack = new ItemStack(Material.INK_SACK, 1);
            final boolean isOn = SETTING_ON.equals(currentValue);
            stack.setDurability(isOn ? DYE_LIME_GREEN : DYE_ROSE_RED);
            return stack;
        } else {
            return new ItemStack(Material.PAPER, 1);
        }
    }

    private List<String> createLore(
            final Localizable description,
            final Localizable currentText,
            final Player viewer
    ) {
        final List<String> lore = new ArrayList<>();

        final String descriptionText = description.render(viewer).toLegacyText();
        if (descriptionText.length() > MAX_LENGTH) {
            lore.addAll(wrapText(descriptionText, MAX_LENGTH));
        } else {
            lore.add(descriptionText);
        }
        lore.add("");
        lore.add(TWO_PART_FORMAT.with(Messages.SETTINGS_MENU_CURRENT.with(ChatColor.GRAY),
                currentText).render(viewer).toLegacyText());
        return lore;
    }

    private static List<String> wrapText(final String text, final int maxLength) {
        final List<String> lines = new ArrayList<>();
        final String[] words = text.split(" ");
        final StringBuilder currentLine = new StringBuilder();

        for (final String word : words) {
            final int projectedLength = currentLine.length() + word.length() + (currentLine.isEmpty() ? 0 : 1);

            if (projectedLength > maxLength) {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine.setLength(0);
                }

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