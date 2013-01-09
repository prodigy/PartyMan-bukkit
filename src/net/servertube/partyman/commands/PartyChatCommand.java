/*
 * Copyright (C) 2012 Sebastian "prodigy" G. <sebastian.gr at servertube.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.servertube.partyman.commands;

import java.text.MessageFormat;
import net.servertube.partyman.PartyInstance;
import net.servertube.partyman.PartyMan;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @since 10.09.2012
 * @version 0.01a 10.09.2012
 * @author Sebastian "prodigy" G. <sebastian.gr at servertube.net>
 */
public class PartyChatCommand implements CommandExecutor {

  private PartyMan plugin;

  public PartyChatCommand(PartyMan plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
    if (cs instanceof Player) {
      if (strings.length > 0) {
        String message = "";
        for (int i = 0; i < strings.length; i++) {
          message += (strings[i]) + " ";
        }
        if (plugin.getPartyManager().getPlayersParty(cs.getName()) == null) {
          ((Player) cs).chat(message);
          PartyInstance.sendPlayerMessage(cs.getName(), "You are not in a party!");
          return true;
        }
        if (plugin.getPartyManager().getPartyChatMode(cs.getName())) {
          ((Player) cs).chat(message);
          return true;
        }
        MessageFormat format = new MessageFormat(plugin.getConfiguration().getString("partyman.partychat.prefix"));
        Object[] args = {plugin.getPartyManager().getPlayersParty(cs.getName()).getName()};
        String _message = PartyInstance.colorizeMessage(format.format(args) + "<" + PartyInstance.getColoredName(cs.getName()) + "> " + message);
        Bukkit.getConsoleSender().sendMessage(_message);
        plugin.getPartyManager().getPlayersParty(cs.getName()).sendPartyChat(cs.getName(), message.toString());
        return true;
      } else {
        if (plugin.getPartyManager().getPlayersParty(cs.getName()) == null) {
          PartyInstance.sendPlayerMessage(cs.getName(), "You are not in a party!");
          return true;
        }
        plugin.getPartyManager().setPartyChatMode(cs.getName(), !plugin.getPartyManager().getPartyChatMode(cs.getName()));
        return true;
      }
    }
    return false;
  }
}
