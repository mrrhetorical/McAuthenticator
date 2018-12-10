package com.rhetorical.auth.signing;

import com.rhetorical.auth.AuthPlayer;
import com.rhetorical.auth.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AccountManager implements Listener {

    protected List<SignInProcess> processList;
    public ArrayList<AuthPlayer> onlinePlayers;

    public AccountManager() {
        this.processList = new ArrayList();
        this.onlinePlayers = new ArrayList();
        Bukkit.getServer().getPluginManager().registerEvents(this, Main.getPlugin(Main.class));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        SignInProcess login = new SignInProcess(e.getPlayer());
    }
}
