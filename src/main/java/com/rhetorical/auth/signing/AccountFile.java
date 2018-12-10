package com.rhetorical.auth.signing;

import com.rhetorical.auth.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class AccountFile {

    public static FileConfiguration account;
    public static File accountFile;

    public static void setup(Plugin p) {
        if (!p.getDataFolder().exists())
            p.getDataFolder().mkdir();

        File file = new File(p.getDataFolder(), "accounts.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Main.console.sendMessage("§cCould not create \"accounts.yml\" file!");
            } catch (Exception e) {
                Main.console.sendMessage("§cCould not access \"accounts.yml\" file!");
            }
        }
        accountFile = file;

        reloadData();
    }

    public static void reloadData() {
        account = YamlConfiguration.loadConfiguration(accountFile);
    }

    public static FileConfiguration getData() {
        return account;
    }

    public static void saveData() {
        try {
            account.save(accountFile);
        } catch(Exception e) {
            Main.console.sendMessage("§cCould not save \"accounts.yml\" file!");
        }

        reloadData();
    }

}
