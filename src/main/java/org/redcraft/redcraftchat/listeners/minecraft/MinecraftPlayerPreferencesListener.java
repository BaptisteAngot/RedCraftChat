package org.redcraft.redcraftchat.listeners.minecraft;

import org.redcraft.redcraftchat.database.PlayerPreferencesManager;

import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class MinecraftPlayerPreferencesListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLogin(PostLoginEvent event) {
        // This will create player preferences if it does not exist already
        PlayerPreferencesManager.getPlayerPreferences(event.getPlayer());
    }
}
