
package net.avicus.compendium.settings.command;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.avicus.compendium.Paginator;
import net.avicus.compendium.TextStyle;
import net.avicus.compendium.commands.exception.InvalidPaginationPageException;
import net.avicus.compendium.commands.exception.MustBePlayerCommandException;
import net.avicus.compendium.commands.exception.TranslatableCommandErrorException;
import net.avicus.compendium.locale.text.Localizable;
import net.avicus.compendium.locale.text.LocalizedNumber;
import net.avicus.compendium.locale.text.UnlocalizedFormat;
import net.avicus.compendium.locale.text.UnlocalizedText;
import net.avicus.compendium.plugin.Messages;
import net.avicus.compendium.settings.PlayerSettings;
import net.avicus.compendium.settings.Setting;
import net.avicus.compendium.settings.SettingValue;
import net.avicus.compendium.settings.SettingValueToggleable;
import net.avicus.compendium.settings.menu.SettingsMenu;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SettingCommands {

  @Command(aliases = {
      "set"}, desc = "Set a setting to a specific value.", usage = "<name> <value>", min = 2)
  public static void set(CommandContext args, CommandSender sender)
      throws TranslatableCommandErrorException {
    MustBePlayerCommandException.ensurePlayer(sender);

    Player player = (Player) sender;

    String query = args.getString(0);

    Optional<Setting> search = Setting.search(sender, query, PlayerSettings.settings());

    if (!search.isPresent()) {
      throw new TranslatableCommandErrorException(Messages.ERRORS_SETTINGS_HELP);
    }

    Setting<Object> setting = search.get();

    Optional<SettingValue> value = (Optional<SettingValue>) setting.getType()
        .parse(args.getString(1));

    if (!value.isPresent()) {
      sender.sendMessage(Messages.ERRORS_INVALID_VALUE.with(ChatColor.RED));
      return;
    }

    PlayerSettings.store().set(player.getUniqueId(), setting, value.get().raw());

    Localizable name = setting.getName().duplicate();
    Localizable set = new UnlocalizedText(value.get().serialize());
    sender.sendMessage(Messages.GENERIC_SETTING_SET.with(ChatColor.GOLD, name, set));
  }

  @Command(aliases = {
      "setting"}, desc = "See a setting's value and information.", min = 1, usage = "<name>", flags = "o:")
  public static void setting(CommandContext args, CommandSender sender)
      throws TranslatableCommandErrorException {
    final Player target = args.hasFlag('o') && sender.hasPermission("settings.other.view")
        ? Bukkit.getPlayer(args.getFlag('o'), sender)
        : MustBePlayerCommandException.ensurePlayer(sender);

    String query = args.getString(0);

    Optional<Setting> search = Setting.search(sender, query, PlayerSettings.settings());

    if (!search.isPresent()) {
      throw new TranslatableCommandErrorException(Messages.ERRORS_SETTINGS_HELP);
    }

    Setting<Object> setting = search.get();

    // Header
    UnlocalizedText line = new UnlocalizedText("--------------",
        TextStyle.ofColor(ChatColor.RED).strike());
    UnlocalizedFormat header = new UnlocalizedFormat("{0} {1} {2}");
    Localizable name = setting.getName().duplicate();
    sender.sendMessage(header.with(ChatColor.YELLOW, line, name, line));

    // Summary
    Localizable summary = setting.getSummary().duplicate();
    summary.style().color(ChatColor.WHITE);
    sender.sendMessage(Messages.GENERIC_SUMMARY.with(ChatColor.YELLOW, summary));

    if (setting.getDescription().isPresent()) {
      // Description
      Localizable desc = setting.getDescription().get().duplicate();
      desc.style().color(ChatColor.WHITE);
      sender.sendMessage(Messages.GENERIC_DESCRIPTION.with(ChatColor.YELLOW, desc));
    }

    // Current value
    Object currentRaw = PlayerSettings.store().get(target.getUniqueId(), setting);
    String current = setting.getType().value(currentRaw).serialize();
    Localizable currentText = new UnlocalizedText(current, ChatColor.WHITE);

    sender.sendMessage(Messages.GENERIC_CURRENT.with(ChatColor.YELLOW, currentText));

    // Default value
    Object defaultRaw = setting.getDefaultValue();
    String def = setting.getType().value(defaultRaw).serialize();
    Localizable defText = new UnlocalizedText(def, ChatColor.WHITE);

    sender.sendMessage(Messages.GENERIC_DEFAULT.with(ChatColor.YELLOW, defText));

    if (setting.getType().value(setting.getDefaultValue()) instanceof SettingValueToggleable
        && target.getName().equals(sender.getName())) {
      Localizable toggle = Messages.GENERIC_TOGGLE.with(ChatColor.YELLOW);
      toggle.style().italic();
      toggle.style().click(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
          "/toggle " + name.render(sender).toPlainText()));

      sender.sendMessage(toggle);
    }
  }

  @Command(aliases = {"settings", "options"}, desc = "Settings menu")
  public static void settings(CommandContext args, CommandSender sender)
      throws TranslatableCommandErrorException {
    MustBePlayerCommandException.ensurePlayer(sender);

    SettingsMenu.create((Player) sender).open();
  }

  @Command(aliases = {"toggle"}, desc = "Toggle a setting between values.", min = 1, max = 1)
  public static void toggle(CommandContext args, CommandSender sender)
      throws TranslatableCommandErrorException {
    MustBePlayerCommandException.ensurePlayer(sender);

    String query = args.getString(0);

    Optional<Setting> search = Setting.search(sender, query, PlayerSettings.settings());

    if (!search.isPresent()) {
      throw new TranslatableCommandErrorException(Messages.ERRORS_SETTINGS_HELP);
    }

    Setting<Object> setting = search.get();

    Optional<Object> result = PlayerSettings.store()
        .toggle(((Player) sender).getUniqueId(), setting);

    if (result.isPresent()) {
      Localizable name = setting.getName().duplicate();
      Localizable value = new UnlocalizedText(setting.getType().value(result.get()).serialize());

      sender.sendMessage(Messages.GENERIC_SETTING_SET.with(ChatColor.GOLD, name, value));
    } else {
      sender.sendMessage(Messages.ERRORS_NOT_TOGGLE.with(ChatColor.RED));
    }
  }
}
