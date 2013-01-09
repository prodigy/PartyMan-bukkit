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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 *
 * @since 10.09.2012
 * @version 0.01a 10.09.2012
 * @author Sebastian "prodigy" G. <sebastian.gr at servertube.net>
 */
public class PartyEventListener implements Listener {

  private PartyMan plugin;

  public PartyEventListener(PartyMan plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerChat(final AsyncPlayerChatEvent event) {
    if (event.isCancelled() || !plugin.isEnabled()) {
      return;
    }
    PartyManager partyManager = plugin.getPartyManager();
    String player = event.getPlayer().getName();
    if (partyManager.getPlayersParty(player) == null || !partyManager.getPartyChatMode(player)) {
      return;
    }
    MessageFormat format = new MessageFormat(PartyMan.getPartyMan().getConfiguration().getString("partyman.partychat.prefix"));
    Object[] args = {partyManager.getPlayersParty(player).getName()};
    String _message = PartyInstance.colorizeMessage(format.format(args) + "<" + PartyInstance.getColoredName(player) + "> " + event.getMessage());
    Bukkit.getConsoleSender().sendMessage(_message);
    partyManager.getPlayersParty(player).sendPartyChat(player, event.getMessage());
    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerDamage(final EntityDamageByEntityEvent event) {
    if (event.isCancelled() || !plugin.isEnabled()) {
      return;
    }
    if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
      Player sender = (Player) event.getDamager();
      Player receiver = (Player) event.getEntity();
      if(sender == null || receiver == null) {
        return;
      }
      PartyInstance party = PartyMan.getPartyMan().getPartyManager().getPlayersParty(sender.getName());
      if(party == null) {
        return;
      }
      if (party.isInParty(receiver.getName())) {
        if (!party.isPvPEnabled()) {
          event.setCancelled(true);
        }
      }
    }
  }
}
