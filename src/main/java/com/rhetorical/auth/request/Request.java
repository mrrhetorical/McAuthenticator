package com.rhetorical.auth.request;

import com.rhetorical.auth.AuthFile;
import com.rhetorical.auth.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Request implements Listener {

    public enum RequestType {
        SIGN_UP, LOG_IN
    }

    private final long id;

    private Player requester;
    private String requestEmail;

    private String key;
    private boolean timesUp;

    private boolean authenticated;

    private RequestType requestType;

    private Thread requestThread;

    public Request(Player requester, String requestEmail, RequestType requestType) {

        Bukkit.getServer().getPluginManager().registerEvents(this, Main.getPlugin());
        this.id  = System.currentTimeMillis();
        this.key = this.generateKey();
        this.requester = requester;
        this.requestEmail = requestEmail;
        this.timesUp = false;
        this.requestType = requestType;

        this.authenticated = false;

        waitTask();

        putInJail(this.requester);
    }

    private ItemStack[] pInvContents;
    private ItemStack[] pArmorContents;
    private Location pLocation;
    private double pHealth;
    private int pHunger;

    private void putInJail(Player p) {

        pInvContents = p.getInventory().getContents();
        pArmorContents = p.getInventory().getArmorContents();
        pLocation = p.getLocation();
        pHealth = p.getHealth();
        pHunger = p.getFoodLevel();

        if (Main.jailLocation != null) {
            p.teleport(Main.jailLocation);
        }
        p.getInventory().clear();
    }

    private void resetPlayer(Player p) {
        p.getInventory().setContents(pInvContents);
        p.getInventory().setArmorContents(pArmorContents);
        p.teleport(pLocation);
        p.setHealth(pHealth);
        p.setFoodLevel(pHunger);
    }

    private String generateKey() {

        String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            int index = (int) Math.floor((Math.random() * charset.length() - 1) + 1);
            sb.append(charset.charAt(index));
        }

        return sb.toString();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if(event.getPlayer() != requester)
            return;

        if (authenticated)
            return;

        event.setCancelled(true);
        event.getPlayer().teleport(Main.jailLocation);
    }

    @EventHandler
    public void onPlayerInputKey(AsyncPlayerChatEvent event) {
        if (event.getPlayer() != requester)
            return;

        if (authenticated)
            return;

        event.setCancelled(true);
        Player p = event.getPlayer();

        if (this.timesUp) {
            p.sendMessage("§cTime is up to input your key! You must create a new request!");
            resetPlayer(this.requester);
            if (Main.currentRequests.containsKey(p) && Main.currentRequests.get(p).equals(this)) {
                Main.currentRequests.remove(this);
            }
        }

        String input = event.getMessage();
        if (input.equals(this.key)) {
            if (!Main.authenticatedPlayers.contains(p))
                Main.authenticatedPlayers.add(p);
            requester.sendMessage("§aSuccessfully authenticated! You are now signed in.");
            this.authenticated = true;
            this.resetPlayer(this.requester);
            if (this.requestType == RequestType.SIGN_UP) {
                AuthFile.getData().set(this.requester.getName() + ".email", this.requestEmail);
                List<String> ipList = new ArrayList<String>();
                ipList.add(requester.getAddress().getAddress().getHostAddress());
                AuthFile.getData().set(this.requester.getName() + ".ips", ipList);
                AuthFile.saveData();
            }
            if (this.requestType == RequestType.LOG_IN) {
                List<String> ipList = AuthFile.getData().getStringList(this.requester.getName() + ".ips");
                String ip = requester.getAddress().getAddress().getHostAddress();
                if (!ipList.contains(ip)) {
                    ipList.add(ip);
                    AuthFile.getData().set(this.requester.getName() + ".ips", ipList);
                    AuthFile.saveData();
                }
            }

            if (Main.currentRequests.containsKey(this)){
                Main.currentRequests.remove(this);
            }

            requestThread.stop();

            System.gc();

        } else {
            requester.sendMessage("§cThat isn't the requested key! Send it again! (Key is CaSE SenSiTIVe)");
        }
    }

    private void waitTask() {
        int timeLimit = this.requestType.equals(RequestType.LOG_IN) ? Main.timeLimitLogIn : Main.timeLimitSignUp * 60;

        requestThread = new Thread(() -> {

            Email email = new Email(this.requester, this.requestEmail, this.key, this.requestType);

            int failedAttempts = 0;
            while(!email.send()) {

                requester.sendMessage("§cConfirmation email failed to send! Attempting to resend. . .");
                if (Main.currentRequests.containsKey(this)){
                    Main.currentRequests.remove(this);
                }

                try{ TimeUnit.SECONDS.sleep(2L); } catch(Exception ignored) {}

                failedAttempts++;
                if(failedAttempts > 5) {
                    Main.console.sendMessage("§cFailed to send confirmation email to " + requester.getDisplayName() + " " + failedAttempts + " times. Make sure that the email information is set up correctly for your email. If you're still having issues, use 'default' and 'default' for both username and password settings in the config.");
                }
            }

            requester.sendMessage("§fConfirmation email sent! Check your email and input your key below: ");



            int elapsedTime = 0;
            while (elapsedTime < timeLimit) {
                try{
                    TimeUnit.SECONDS.sleep(1L);
                } catch(Exception ignored) {}
                elapsedTime++;
            }

            this.timesUp = true;
        });

        requestThread.start();
    }

    public long getId() {
        return this.id;
    }
}
