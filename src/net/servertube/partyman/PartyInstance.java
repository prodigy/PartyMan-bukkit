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
package net.servertube.partyman;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 *
 * @since 10.09.2012
 * @version 0.01a 10.09.2012
 * @author Sebastian "prodigy" G. <sebastian.gr at servertube.net>
 */
public class PartyInstance {

  private PartyType type;
  private String leader;
  private ArrayList<String> players;
  private String name;
  private String abbr;
  private Server server;
  private String pexgroup;
  private boolean pvpEnabled;

  public PartyInstance(PartyType type, String name, String abbr, String creator) {
    this.type = type;
    this.name = name;
    this.abbr = abbr;
    this.leader = creator;
    this.players = new ArrayList<String>();
    this.players.add(leader);
    this.server = PartyMan.getPartyMan().getServer();
    this.pvpEnabled = true;
    String parent = PartyMan.getPartyMan().getConfiguration().getString("partyman.parentgroup");
    pexgroup = parent + "_" + name.toLowerCase();
    String prefix = PartyMan.getPartyMan().getConfiguration().getString("partyman.group.prefix");
    String suffix = PartyMan.getPartyMan().getConfiguration().getString("partyman.group.suffix");
    server.dispatchCommand(server.getConsoleSender(), "pex group " + pexgroup + " create");
    server.dispatchCommand(server.getConsoleSender(), "pex group " + pexgroup + " parents set " + parent);
    server.dispatchCommand(server.getConsoleSender(), "pex group " + pexgroup + " prefix " + prefix + abbr + suffix);
    server.dispatchCommand(server.getConsoleSender(), "pex group " + pexgroup + " user add " + leader);
    sendPlayerMessage(leader, "Party created. Start inviting others!");
    PartyMan.getPartyMan().getSavedParties().createSection("parties." + name);
    PartyMan.getPartyMan().getSavedParties().set("parties." + name + ".abbr", abbr);
    PartyMan.getPartyMan().getSavedParties().set("parties." + name + ".leader", leader);
    Boolean chatMode = PartyMan.getPartyMan().getSavedParties().getBoolean("players." + leader + ".chat");
    if (chatMode == null) {
      chatMode = false;
    }
    PartyMan.getPartyMan().getSavedParties().createSection("players." + leader);
    PartyMan.getPartyMan().getSavedParties().set("players." + leader + ".party", this.name);
    PartyMan.getPartyMan().getPartyManager().setPartyChatMode(leader, chatMode);
    PartyMan.getPartyMan().saveConfigs();
  }

  public synchronized void setPvPEnabled(boolean enabled) {
    this.pvpEnabled = enabled;
    this.sendPartyMessage("PvP " + (enabled ? "&aEnabled" : "&cDisabled") + "&f!");
    PartyMan.getPartyMan().getSavedParties().set("parties." + name + ".pvp", this.pvpEnabled);
    PartyMan.getPartyMan().saveConfigs();
  }

  public synchronized boolean isPvPEnabled() {
    return this.pvpEnabled;
  }

  public final synchronized void addPlayer(String player) {
    if (player == null) {
      return;
    }
    if (players.size() >= PartyMan.getPartyMan().getConfiguration().getInt("partyman.max-members", 3)) {
      sendPartyMessage("Maximum member limit reached!");
      sendPlayerMessage(player, "Maximum member limit reached!");
      return;
    }
    if (!this.players.contains(player)) {
      sendPartyMessage(getColoredName(player) + " joined the party.");
      sendPlayerMessage(player, colorizeMessage(getPartyMessagePrefix() + " you joined the party. " + getPartyMessageSuffix()));
      server.dispatchCommand(server.getConsoleSender(), "pex group " + pexgroup + " user add " + player);
      this.players.add(player);
      Boolean chatMode = PartyMan.getPartyMan().getSavedParties().getBoolean("players." + player + ".chat");
      if (chatMode == null) {
        chatMode = false;
      }
      PartyMan.getPartyMan().getSavedParties().createSection("players." + player);
      PartyMan.getPartyMan().getSavedParties().set("players." + player + ".party", this.name);
      PartyMan.getPartyMan().getPartyManager().setPartyChatMode(player, chatMode);
      PartyMan.getPartyMan().saveConfigs();
    }
  }

  public synchronized void removePlayer(String player, boolean kicked) {
    if (player == null) {
      return;
    }
    if (!this.players.contains(player)) {
      return;
    }
    this.players.remove(player);
    if (this.leader.equals(player)) {
      setPartyLeader(this.players.get(0));
    }
    server.dispatchCommand(server.getConsoleSender(), "pex group " + pexgroup + " user remove " + player);
    if (!kicked) {
      sendPartyMessage(getColoredName(player) + " left the party.");
      sendPlayerMessage(player, "You left the party.");
    } else {
      sendPartyMessage(getColoredName(player) + " was kicked from the party.");
      sendPlayerMessage(player, "You were kicked from the party.");
    }
    PartyMan.getPartyMan().getSavedParties().set("players." + player, null);
    PartyMan.getPartyMan().saveConfigs();
  }

  public synchronized void sendPartyMessage(String message) {
    String _message = colorizeMessage(getPartyMessagePrefix() + message + getPartyMessageSuffix());
    Player _player = null;
    for (String player : players) {
      _player = PartyMan.getPartyMan().getPlayer(player);
      if (_player != null) {
        _player.sendMessage(_message);
      }
    }
  }

  public synchronized void sendPartyChat(String sender, String message) {
    MessageFormat format = new MessageFormat(PartyMan.getPartyMan().getConfiguration().getString("partyman.partychat.prefix"));
    Object[] args = {name};
    String _message = colorizeMessage(format.format(args) + "<" + getColoredName(sender) + "> " + message + getPartyChatSuffix());
    Player _player = null;
    for (String player : players) {
      _player = PartyMan.getPartyMan().getPlayer(player);
      if (_player != null) {
        _player.sendMessage(_message);
      }
    }
  }

  public synchronized PartyType getPartyType() {
    return type;
  }

  public synchronized boolean isInParty(String player) {
    if (player == null) {
      return false;
    }
    if (players.contains(player)) {
      return true;
    }
    return false;
  }

  public synchronized boolean isLeader(String player) {
    if (player == null) {
      return false;
    }
    if (leader.equals(player)) {
      return true;
    }
    return false;
  }

  public synchronized String getLeader() {
    return leader;
  }

  public synchronized String getName() {
    return name;
  }

  public synchronized String getAbbr() {
    return abbr;
  }

  public synchronized void clearParty() {
    sendPartyMessage("Party is being disbanded.");
    for (String player : players) {
      PartyMan.getPartyMan().getSavedParties().set("players." + player.toLowerCase(), null);
    }
    this.players.clear();
    PartyMan.getPartyMan().getSavedParties().set("parties." + name, null);
    server.dispatchCommand(server.getConsoleSender(), "pex group " + pexgroup + " delete");
    PartyMan.getPartyMan().saveConfigs();
  }

  public synchronized void shutdownDisband() {
    this.players.clear();
    server.dispatchCommand(server.getConsoleSender(), "pex group " + pexgroup + " delete");
  }

  public synchronized void setPartyLeader(String leader) {
    if (leader == null) {
      return;
    }
    this.leader = leader;
    sendPartyMessage(getColoredName(leader) + " is now the party leader.");
    PartyMan.getPartyMan().getSavedParties().set("parties." + getName() + ".leader", leader);
    PartyMan.getPartyMan().saveConfigs();
  }

  public synchronized ArrayList<String> getPlayers() {
    return players;
  }

  public static synchronized String colorizeMessage(String message) {
    return ChatColor.translateAlternateColorCodes('&', message);
  }

  public static synchronized String getPartyMessagePrefix() {
    String prefix = PartyMan.getPartyMan().getConfiguration().getString("partyman.partymessage.prefix");
    if (prefix != null && prefix.length() > 0) {
      return prefix;
    }
    return "";
  }

  public static synchronized String getPartyMessageSuffix() {
    String suffix = PartyMan.getPartyMan().getConfiguration().getString("partyman.partymessage.suffix");
    if (suffix != null && suffix.length() > 0) {
      return suffix;
    }
    return "";
  }

  public static synchronized void sendPlayerMessage(String player, String message) {
    Player _player = PartyMan.getPartyMan().getPlayer(player);
    if (_player != null) {
      _player.sendMessage(colorizeMessage(getPartyMessagePrefix() + message + getPartyMessageSuffix()));
    }
  }

  private String getPartyChatPrefix() {
    String prefix = PartyMan.getPartyMan().getConfiguration().getString("partyman.partychat.prefix");
    if (prefix != null && prefix.length() > 0) {
      return prefix;
    }
    return "";
  }

  private String getPartyChatSuffix() {
    String suffix = PartyMan.getPartyMan().getConfiguration().getString("partyman.partychat.suffix");
    if (suffix != null && suffix.length() > 0) {
      return suffix;
    }
    return "";
  }

  public static synchronized String getColoredName(String player) {
    return PermissionsEx.getPermissionManager().getUser(player).getPrefix() + PermissionsEx.getPermissionManager().getUser(player).getName() + PermissionsEx.getPermissionManager().getUser(player).getSuffix();
  }
}
