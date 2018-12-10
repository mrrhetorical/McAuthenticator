package com.rhetorical.auth.signing;

import com.rhetorical.auth.AuthPlayer;
import com.rhetorical.auth.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

public class SignInProcess implements Listener {

    private AuthPlayer signing;

    public SignInProcess(Player p) {

        Bukkit.getServer().getPluginManager().registerEvents(this, Main.getPlugin(Main.class));

        this.signing = new AuthPlayer(p);
        signing.getAuthState();
        signing.parent.sendMessage("§fEnter username:");

        checkSignedIn(p);
    }

    public void checkSignedIn(Player p) {
        Thread t = new Thread(() -> {
            while (!signing.isAuthenticated()) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch(Exception ignored) {}
            }
        });

        t.start();
    }

    @EventHandler
    public void onEnterText(AsyncPlayerChatEvent e) {
        if (e.getPlayer() != signing)
            return;

        if (signing.getAuthState() != SignInState.COMPLETE)
            e.setCancelled(true);

        if (signing.getAuthState().equals(SignInState.AWAITING_USERNAME)) {
            String username = e.getMessage();

            if (!AccountFile.getData().contains("Users." + signing.parent.getUniqueId())) {
                signing.parent.sendMessage("§cThat username is not tied to your account!");
                signing.parent.sendMessage("§cIf you have not yet created an account, please sign up using \"§f/mcAuth signUp (account name) {password}§c\".");
                return;
            }

            if (AccountFile.getData().getString("Users." + signing.parent.getUniqueId() + ".username").equals(Obfuscator.obfuscate(username))) {
                signing.parent.sendMessage("§fEnter password:");
                signing.proceedToPassword();
                return;
            }

        } else if (signing.getAuthState().equals(SignInState.AWAITING_PASSWORD)) {
            String password = e.getMessage();
            if (Obfuscator.obfuscate(password).equals(AccountFile.getData().getString("Users." + signing.parent.getUniqueId() + ".password"))) {
                signing.proceedToTwoFactor();
                return;
            } else {
                signing.parent.sendMessage("§cThe password does not match the username!");
                return;
            }
        } else if (signing.getAuthState().equals(SignInState.AWAITING_2FA)) {
            signing.authenticate(); // TEMP
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        if (e.getPlayer() == signing) {
            signing = null;
            System.gc();
        }
    }
}
