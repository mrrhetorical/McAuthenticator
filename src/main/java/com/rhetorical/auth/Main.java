package com.rhetorical.auth;

import com.rhetorical.auth.request.Request;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main extends JavaPlugin implements Listener {
    /* Process:
     * - Send email after input of email.
     * - Input confirmation email.
     * - Whenever logging on to the server, get email confirmation for signing in.
     * - (10 minute timeout on the email confirmation).
     *
     * */

    public static ConsoleCommandSender console;
    public static String serverName; //To use in sending emails messages
    public static String authEmail; //Email to use to send email messages
    public static String authPassword; //Password to use for the email.
    public static int timeLimitSignUp; // Time limit to authenticate in minutes
    public static int timeLimitLogIn; // Time limit to authenticate in seconds for log in
    private static String signUpTemplate = "Thank you for signing up with {SERVER_NAME}, {PLAYER}. \nYour confirmation code: \n{KEY}";
    private static String logInTemplate = "{PLAYER}, \nThis is your log in key for {SERVER_NAME}: \n{KEY}";
    private static String subjectTemplate = "{SERVER_NAME} Two-Factor Authentication";
    public static Location jailLocation;

    public static HashMap<Player, Request> currentRequests = new HashMap<>();
    public static ArrayList<Player> authenticatedPlayers = new ArrayList<>();

    public static Plugin getPlugin() {
        return Bukkit.getServer().getPluginManager().getPlugin("E-2FA");
    }

    @Override
    public void onEnable() {
        console = Bukkit.getConsoleSender();
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        AuthFile.setup(this);

        getPlugin().saveDefaultConfig();
        getPlugin().reloadConfig();

        serverName = getPlugin().getConfig().getString("server_name");
        authEmail = getPlugin().getConfig().getString("auth_email");
        authPassword = getPlugin().getConfig().getString("auth_password");
        timeLimitSignUp = getPlugin().getConfig().getInt("timeout.sign_up");
        timeLimitLogIn = getPlugin().getConfig().getInt("timeout.log_in");
        signUpTemplate = getPlugin().getConfig().getString("template.sign_up");
        logInTemplate = getPlugin().getConfig().getString("template.log_in");
        subjectTemplate = getPlugin().getConfig().getString("template.subject");
        jailLocation = (Location) getPlugin().getConfig().get("jail_location");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!label.equalsIgnoreCase("auth"))
            return false;

        if (!(sender instanceof Player)) {
            if (label.equalsIgnoreCase("reload")) {
                getPlugin().saveConfig();
                getPlugin().reloadConfig();
                sender.sendMessage("§aSuccessfully reloaded McAuthenticator's config! (Restart the server if it doesn't take effect.)");
            }
            return true;
        }

        Player p = (Player) sender;

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("signUp")) {
                if (AuthFile.getData().contains(p.getName() + ".email")) {
                    p.sendMessage("§cCan't sign up if you already have an account!");
                    p.sendMessage("§cPlease sign in using your existing account's authenticator.");
                    p.sendMessage("§cIf you can not access your email, please contact proof that it is your email,");
                    p.sendMessage("§cAnd contact a server moderator or administrator.");
                    return true;
                }

                Request r;
                try {
                    if (AuthFile.contains(args[1])) {
                        p.sendMessage("§cThere is already an account registered with that email address!");
                        return true;
                    }

                    r = new Request(p, args[1], Request.RequestType.SIGN_UP);
                } catch(Exception e) {
                    p.sendMessage("§cIncorrect format! Correct format: \"/auth signUp {email}\"!");
                    return true;
                }

                currentRequests.put(p, r);
            } else if (args[0].equalsIgnoreCase("jail") && (p.hasPermission("mcauth.setjail") || p.isOp() || p.hasPermission("mcauth.*"))) {
                jailLocation = p.getLocation();
                getPlugin().getConfig().set("jail_location", jailLocation);
                getPlugin().saveConfig();
                getPlugin().reloadConfig();
                p.sendMessage("§aSet jail location!");
                return true;
            } else if (args[0].equalsIgnoreCase("reload") && (p.hasPermission("mcauth.reload") || p.isOp() || p.hasPermission("mcauth.*"))) {

                getPlugin().saveConfig();
                getPlugin().reloadConfig();
                p.sendMessage("§aSuccessfully reloaded McAuthenticator's config! (Restart the server if it doesn't take effect.)");
                return true;
            }
        }

        return true;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        String ip = p.getAddress().getAddress().getHostAddress();
        console.sendMessage("§a" + p.getName() + " connected with ip: " + ip);
        if (AuthFile.getData().contains(p.getName() + ".email")) {

            if (AuthFile.getData().contains(p.getName() + ".ips")) {
                List<String> ipList = AuthFile.getData().getStringList(p.getName() + ".ips");
                if (ipList.contains(ip)) {
                    p.sendMessage("§aSigned in using recognized ip!");
                    Main.authenticatedPlayers.add(p);
                    return;
                }
            }


            Request r = new Request(p, AuthFile.getData().getString(p.getName() + ".email"), Request.RequestType.LOG_IN);
            currentRequests.put(p, r);
            return;
        }

        p.sendMessage("§cYou haven't signed up for an account with this server yet!");
        p.sendMessage("§cPlease use the command \"/auth signUp {email}\" to sign up for an account!");
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if (Main.authenticatedPlayers.contains(event.getPlayer()))
            Main.authenticatedPlayers.remove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!authenticatedPlayers.contains(event.getPlayer()))
            event.setCancelled(true);
    }



    public static String getSignUpEmailTemplate() {
        return Main.signUpTemplate;
    }

    public static String getLogInEmailTemplate() {
        return Main.logInTemplate;
    }

    public static String getSubjectTemplate() {
        return Main.subjectTemplate;
    }
}
