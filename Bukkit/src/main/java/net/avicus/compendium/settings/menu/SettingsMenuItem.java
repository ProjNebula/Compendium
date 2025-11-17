package net.avicus.compendium.settings.menu;

import net.avicus.compendium.locale.text.UnlocalizedFormat;
import net.avicus.compendium.menu.IndexedMenuItem;
import net.avicus.compendium.menu.inventory.ClickableInventoryMenuItem;
import net.avicus.compendium.menu.inventory.InventoryMenuItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public abstract class SettingsMenuItem implements ClickableInventoryMenuItem, IndexedMenuItem,
        InventoryMenuItem {

    protected static final int MAX_LENGTH = 50;
    protected static final UnlocalizedFormat TWO_PART_FORMAT = new UnlocalizedFormat("{0}: {1}");
    protected final Player viewer;
    protected final SettingsMenu parent;
    protected final int index;

    protected SettingsMenuItem(final Player viewer, final SettingsMenu parent, final int index) {
        this.viewer = viewer;
        this.parent = parent;
        this.index = index;
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        onClick(event.getClick());
    }

    public abstract void onClick(ClickType type);

    @Override
    public int getIndex() {
        return this.index;
    }
}
