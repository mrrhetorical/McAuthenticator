package com.rhetorical.auth;

import com.rhetorical.auth.signing.AccountFile;
import com.rhetorical.auth.signing.AccountManager;
import com.rhetorical.auth.signing.Obfuscator;
import com.rhetorical.auth.signing.SignInState;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    public static ConsoleCommandSender console;

    public static String prefix = "§f[§6§lMcAuth§r§f]§r ";

    public static String translate_api_key;

    @Override
    public void onEnable() {

        console = Bukkit.getConsoleSender();

        AccountFile.setup(this);

        AccountManager m = new AccountManager();

        this.saveDefaultConfig();
        this.reloadConfig();
        prefix = !this.getConfig().getString("prefix").equals("none") ? this.getConfig().getString("prefix") : prefix;
        translate_api_key = this.getConfig().getString("translation.api_key") != null ? this.getConfig().getString("translation.api_key") : "none";

        console.sendMessage(prefix + "§aMcAuth version §a" + this.getDescription().getVersion() + " §fis now enabled!");
    }

    @Override
    public void onDisable() {
        console.sendMessage(prefix + "§aMcAuth version §a" + this.getDescription().getVersion() + " §fis now safely disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!label.equalsIgnoreCase("mcAuth") && !label.equalsIgnoreCase("auth"))
            return false;

        boolean isPlayer = sender instanceof Player ? true : false;

        AuthPlayer p = null;
        Player player = null;

        if (isPlayer) {
            player = (Player) sender;
            p = new AuthPlayer(player);
        }

        if (args.length == 0) {


        } else if (args.length == 1) {


        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("signUp") && p.hasPerm("McAuth.SignUp")) {
                if (p.getAuthState() != SignInState.COMPLETE && AccountFile.getData().contains("Users." + p.parent.getUniqueId())) {
                    p.parent.sendMessage(Main.prefix + "§cYou must sign in first before you can change your password.");
                    return true;
                }

                String user = Obfuscator.obfuscate(args[1]);
                String pass = Obfuscator.obfuscate(args[2]);

                AccountFile.getData().set("Users." + p.parent.getUniqueId() + ".username", user);
                AccountFile.getData().set("Users." + p.parent.getUniqueId() + ".password", pass);
                AccountFile.saveData();
                return true;
            }
        }

        return true;
    }

}
