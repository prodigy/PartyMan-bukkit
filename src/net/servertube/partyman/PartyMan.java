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

import java.io.File;
import net.servertube.partyman.commands.PartyCommand;
import java.util.logging.Level;
import net.servertube.partyman.commands.PartyChatCommand;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 *
 * @since 10.09.2012
 * @version 0.01a 10.09.2012
 * @author Sebastian "prodigy" G. <sebastian.gr at servertube.net>
 */
public class PartyMan extends JavaPlugin {

  private static PartyMan partyMan;
  private PermissionManager pex;
  private Server server;
  private File configFile;
  private File savedPartiesFile;
  private YamlConfiguration config;
  private YamlConfiguration savedParties;
  private PartyCommand partyCommand;
  private PartyChatCommand partyChatCommand;
  private PartyManager partyManager;
  private PartyEventListener partyEventListener;

  @Override
  public void onEnable() {
    partyMan = this;
    pex = PermissionsEx.getPermissionManager();
    server = this.getServer();
    partyCommand = new PartyCommand(this);
    partyChatCommand = new PartyChatCommand(this);
    getCommand("party").setExecutor(partyCommand);
    getCommand("pc").setExecutor(partyChatCommand);
    savedPartiesFile = new File(getDataFolder(), "parties.yml");
    configFile = new File(getDataFolder(), "config.yml");
    if (!savedPartiesFile.exists()) {
      saveResource("parties.yml", false);
    }
    if (!configFile.exists()) {
      saveResource("config.yml", false);
    }
    config = new YamlConfiguration();
    savedParties = new YamlConfiguration();
    if (!loadConfigs()) {
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }
    saveConfigs();
    partyEventListener = new PartyEventListener(this);
    partyManager = new PartyManager(this);
    getLogger().log(Level.INFO, "PartyMan enabled.");
  }

  @Override
  public void onDisable() {
    partyManager.shutdownDisbandAll();
  }

  protected synchronized void setPartyManager(PartyManager partyManager) {
    this.partyManager = partyManager;
  }

  public synchronized PartyManager getPartyManager() {
    return partyManager;
  }

  public Player getPlayer(String player) {
    return server.getPlayer(player);
  }

  public synchronized void saveConfigs() {
    try {
      savedParties.save(savedPartiesFile);
    } catch (Exception ex) {
      getLogger().log(Level.WARNING, "Could not save parties.yml; All parties may be deleted!");
    }
  }

  public YamlConfiguration getConfiguration() {
    return config;
  }

  public YamlConfiguration getSavedParties() {
    return savedParties;
  }

  public static PartyMan getPartyMan() {
    return partyMan;
  }

  private boolean loadConfigs() {
    try {
      config.load(configFile);
    } catch (Exception ex) {
      getLogger().log(Level.WARNING, "Could not load config.yml; All parties may be deleted!");
      return false;
    }
    try {
      savedParties.load(savedPartiesFile);
    } catch (Exception ex) {
      getLogger().log(Level.WARNING, "Could not load parties.yml; All parties may be deleted!");
      return false;
    }
    return true;
  }
}
