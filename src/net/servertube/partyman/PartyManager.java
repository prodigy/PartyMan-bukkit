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

import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.ChatColor;

/**
 *
 * @since 10.09.2012
 * @version 0.01a 10.09.2012
 * @author Sebastian "prodigy" G. <sebastian.gr at servertube.net>
 */
public class PartyManager {

  private HashMap<String, PartyInstance> parties; //Name, PartyInstance
  private HashMap<String, PartyInstance> playerParties; //Player, PartyInstance
  private HashMap<String, PartyInstance> partyInvites; //Player, PartyInstance
  private HashMap<String, Boolean> partyChatEnabled;
  private PartyMan plugin;
  private int maxNameLen;
  private int maxAbbrLen;

  public PartyManager(PartyMan plugin) {
    this.plugin = plugin;
    plugin.setPartyManager(this);
    this.parties = new HashMap<String, PartyInstance>();
    this.playerParties = new HashMap<String, PartyInstance>();
    this.partyInvites = new HashMap<String, PartyInstance>();
    this.partyChatEnabled = new HashMap<String, Boolean>();
    maxNameLen = PartyMan.getPartyMan().getConfiguration().getInt("partyman.max-name-length", 12);
    maxAbbrLen = PartyMan.getPartyMan().getConfiguration().getInt("partyman.max-abbr-length", 3);
    for (String party : plugin.getSavedParties().getConfigurationSection("parties").getKeys(false)) {
      String abbr = plugin.getSavedParties().getString("parties." + party + ".abbr");
      String leader = plugin.getSavedParties().getString("parties." + party + ".leader");
      Boolean pvp = plugin.getSavedParties().getBoolean("parties." + party + ".pvp");
      if (abbr == null || leader == null) {
        plugin.getLogger().log(Level.WARNING, "Error loading party '" + party + "'; abbr or leader = null!!");
        continue;
      }
      plugin.getLogger().log(Level.INFO, "Reloading Party '" + party + "' (" + abbr + ") with leader: " + leader);
      PartyInstance _party = createParty(PartyType.PUBLIC, party, abbr, leader);
      if (pvp != null) {
        _party.setPvPEnabled(pvp);
      }
    }
    for (String player : plugin.getSavedParties().getConfigurationSection("players").getKeys(false)) {
      String party = plugin.getSavedParties().getString("players." + player + ".party");
      if (party == null) {
        plugin.getLogger().log(Level.WARNING, "Error adding player '" + player + "'; No party defined!!");
        continue;
      }
      plugin.getLogger().log(Level.INFO, "Adding '" + player + "' to: " + party);
      PartyInstance _party = parties.get(party);
      if (_party == null) {
        plugin.getLogger().log(Level.WARNING, "Error adding player '" + player + "' to '" + party + "'; Given party does not exist!!");
        continue;
      }
      _party.addPlayer(player);
      playerParties.put(player, _party);
    }
  }

  public final synchronized PartyInstance createParty(PartyType type, String name, String abbr, String creator) {
    if (name.length() > maxNameLen) {
      name = name.substring(0, maxNameLen);
    }
    if (abbr.length() > maxAbbrLen) {
      abbr = abbr.substring(0, maxAbbrLen);
    }
    if (playerParties.containsKey(creator)) {
      PartyInstance.sendPlayerMessage(creator, "You are already in a party!");
      return null;
    }
    PartyInstance party = new PartyInstance(type, name, abbr, creator);
    this.parties.put(name, party);
    this.playerParties.put(creator, party);
    return party;
  }

  public synchronized void listParties(String requester) {
    String out = "Parties registered:";
    for (PartyInstance party : parties.values()) {
      out += "\n&f<&6" + party.getName() + "&f> [&7" + party.getAbbr() + "&f]\n  Players: ";
      int x = 0;
      for (String player : party.getPlayers()) {
        out += (x < 1 ? "" : ", ") + player;
        ++x;
      }
      out += "\n";
    }
    PartyMan.getPartyMan().getPlayer(requester).sendMessage(ChatColor.translateAlternateColorCodes('&', out));
  }

  public synchronized void disbandParty(String disbander) {
    if (disbander == null) {
      return;
    }
    if (!playerParties.containsKey(disbander)) {
      PartyInstance.sendPlayerMessage(disbander, "You are not in a party!");
      return;
    }
    if (!playerParties.get(disbander).isLeader(disbander)) {
      PartyInstance.sendPlayerMessage(disbander, "You are not the party leader!");
      return;
    }
    PartyInstance party = playerParties.remove(disbander);
    for (String player : party.getPlayers()) {
      playerParties.remove(player);
      partyChatEnabled.remove(player);
      plugin.getSavedParties().set("players." + player, null);
    }
    party.clearParty();
    parties.remove(party.getName());
    plugin.getSavedParties().set("parties." + party.getName(), null);
    plugin.saveConfigs();
    party = null;
  }

  public synchronized void leaveParty(String leaver) {
    if (leaver == null) {
      return;
    }
    if (!playerParties.containsKey(leaver)) {
      PartyInstance.sendPlayerMessage(leaver, "You are not in a party!");
      return;
    }
    playerParties.remove(leaver).removePlayer(leaver, false);
    partyChatEnabled.remove(leaver);
    plugin.getSavedParties().set("players." + leaver, null);
    plugin.saveConfigs();
  }

  public synchronized void sendPartyInvite(String inviter, String invited) {
    if (inviter == null || invited == null) {
      return;
    }
    if (plugin.getPlayer(invited) == null) {
      PartyInstance.sendPlayerMessage(inviter, "Player not found!");
      return; //is the target player available?
    }
    if (getPlayersParty(inviter) == null) {
      PartyInstance.sendPlayerMessage(inviter, "You are not in a party!");
      return; //is inviter in a group?
    }
    if (!getPlayersParty(inviter).isLeader(inviter)) {
      PartyInstance.sendPlayerMessage(inviter, "You are not the party leader!");
      return; //is inviter the leader?
    }
    if (getPlayersParty(invited) != null) {
      PartyInstance.sendPlayerMessage(inviter, "<" + PartyInstance.getColoredName(invited) + "> is already in a party!");
      return; //is invited not in a group?
    }
    PartyInstance party = getPlayersParty(inviter);
    partyInvites.put(invited, party);
    PartyInstance.sendPlayerMessage(invited, "You have been invited to join [&6" + party.getName() + "&f].\nType \"/party accept\" to join the party, or \"/party decline\" to decline the invite!");
  }

  public synchronized void acceptInvitation(String accepter) {
    if (accepter == null) {
      return;
    }
    if (!partyInvites.containsKey(accepter)) {
      PartyInstance.sendPlayerMessage(accepter, "There are no pending invites!");
      return;
    }
    if (getPlayersParty(accepter) != null) {
      PartyInstance.sendPlayerMessage(accepter, "You are already in a party!");
      return;
    }
    PartyInstance party = partyInvites.remove(accepter);
    party.addPlayer(accepter);
    playerParties.put(accepter, party);
  }

  public synchronized void declineInvitation(String denyer) {
    if (denyer == null) {
      return;
    }
    if (!partyInvites.containsKey(denyer)) {
      PartyInstance.sendPlayerMessage(denyer, "There are no pending invites!");
      return;
    }
    PartyInstance party = partyInvites.remove(denyer);
    party.sendPartyMessage("<" + PartyInstance.getColoredName(denyer) + "> declined the invite!");
  }

  public synchronized PartyInstance getPlayersParty(String player) {
    return playerParties.get(player);
  }

  public synchronized boolean isPlayerInParty(String player) {
    return playerParties.containsKey(player);
  }

  public synchronized void shutdownDisbandAll() {
    for (PartyInstance party : parties.values()) {
      party.shutdownDisband();
    }
  }

  public synchronized void setPartyChatMode(String player, boolean active) {
    this.partyChatEnabled.put(player, active);
    PartyInstance.sendPlayerMessage(player, (active ? "&aEnabled" : "&cDisabled") + "&f party chat mode!");
    plugin.getSavedParties().set("players." + player + ".chat", active);
    plugin.saveConfigs();
  }

  public synchronized boolean getPartyChatMode(String player) {
    return this.partyChatEnabled.get(player);
  }

  public synchronized void setNewPartyLeader(String promoter, String newleader) {
    if (promoter == null || newleader == null) {
      return;
    }
    if (plugin.getPlayer(newleader) == null) {
      PartyInstance.sendPlayerMessage(promoter, "Player not found!");
      return; //is the target player available?
    }
    if (getPlayersParty(newleader) == null) {
      PartyInstance.sendPlayerMessage(promoter, "You are not in a party!");
      return; //is promoter in a group?
    }
    if (!getPlayersParty(promoter).isLeader(promoter)) {
      PartyInstance.sendPlayerMessage(promoter, "You are not the party leader!");
      return; //is promoter the leader?
    }
    if (!getPlayersParty(newleader).equals(getPlayersParty(promoter))) {
      PartyInstance.sendPlayerMessage(promoter, "<" + PartyInstance.getColoredName(newleader) + "> is already in a party!");
      return; //is newleader in another group?
    }
    PartyInstance party = getPlayersParty(promoter);
    party.setPartyLeader(newleader);
  }
}
