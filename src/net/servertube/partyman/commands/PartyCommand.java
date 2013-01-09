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

import net.servertube.partyman.PartyInstance;
import net.servertube.partyman.PartyMan;
import net.servertube.partyman.PartyType;
import org.bukkit.ChatColor;
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
public class PartyCommand implements CommandExecutor {

  private PartyMan plugin;

  public PartyCommand(PartyMan plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
    if (strings.length > 0) {
      if (cs instanceof Player) {
        String subcmd = strings[0].toLowerCase();
        if (subcmd.equals("create")) {
          if (strings.length < 3) {
            displayUsage(cs);
            return true;
          }
          String name = strings[1];
          String abbr = strings[2];
          if (name == null || abbr == null) {
            displayUsage(cs);
            return true;
          }
          PartyInstance createParty = plugin.getPartyManager().createParty(PartyType.LEADER, name, abbr, cs.getName());
          return true;
        } else if (subcmd.equals("list")) {
          plugin.getPartyManager().listParties(cs.getName());
          return true;
        } else if (subcmd.equals("disband")) {
          plugin.getPartyManager().disbandParty(cs.getName());
          return true;
        } else if (subcmd.equals("leave")) {
          plugin.getPartyManager().leaveParty(cs.getName());
          return true;
        } else if (subcmd.equals("invite")) {
          if (strings.length < 2) {
            displayUsage(cs);
            return true;
          }
          plugin.getPartyManager().sendPartyInvite(cs.getName(), strings[1]);
          return true;
        } else if (subcmd.equals("accept")) {
          plugin.getPartyManager().acceptInvitation(cs.getName());
          return true;
        } else if (subcmd.equals("decline")) {
          plugin.getPartyManager().declineInvitation(cs.getName());
          return true;
        } else if (subcmd.equals("pvp")) {
          PartyInstance party = plugin.getPartyManager().getPlayersParty(cs.getName());
          party.setPvPEnabled(!party.isPvPEnabled());
          return true;
        } else if (subcmd.equals("leader")) {
          if(strings.length < 2) {
            displayUsage(cs);
            return true;
          }
          plugin.getPartyManager().setNewPartyLeader(cs.getName(), strings[1]);
          return true;
        }
      }
    }
    displayUsage(cs);
    return true;
  }

  public synchronized void displayUsage(CommandSender cs) {
    cs.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&7Party usage instructions:&f\n"
            + "/party &ccreate&f [Name] [Abbreviation] - Create party\n"
            + "/party &cinvite&f [Player] - Invite a player\n"
            + "/party &cpvp&f - Toggle Party PvP Mode\n"
            + "/party &cleader&f [Player] - Makes [Player] the new leader\n"
            + "/party &caccept&f - Accept an invitation\n"
            + "/party &cdecline&f - Decline an invitation\n"
            + "/party &cleave&f - Leave your party\n"
            + "/party &cdisband&f - Disband your party\n"
            + "/party &clist&f - List all open parties\n"));
  }
}
