package com.rhetorical.auth;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;

public class AuthFile {
    
    private static FileConfiguration auth;
    private static File authFile;

    static void setup(Plugin p) {

        if (!p.getDataFolder().exists()) {
            p.getDataFolder().mkdir();
        }

        File file = new File(p.getDataFolder(), "auth.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch(Exception e) {
                Main.console.sendMessage("§cCould not create \"auth.yml\" file!");
            }
        }

        authFile = file;
        reloadData();
    }

    private static void reloadData() {
        auth = YamlConfiguration.loadConfiguration(authFile);
    }

    static boolean contains(String s) {

        try {
            BufferedReader reader = new BufferedReader(new FileReader(authFile));
            while(reader.readLine() != null) {
                if (reader.readLine().contains(s))
                    return true;
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    public static void saveData() {
        try {
            auth.save(authFile);
        } catch(Exception e) {
            Main.console.sendMessage("§cCould not save \"auth.yml\" file!");
        }
        reloadData();
    }

    public static FileConfiguration getData() {
        return auth;
    }
}
