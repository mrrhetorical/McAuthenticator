package com.rhetorical.auth.request;

import com.rhetorical.auth.Main;
import org.bukkit.entity.Player;

import javax.mail.Authenticator;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

class Email {

    private String sender;
    private String recipient;
    private String subject;
    private String contents;

    private String key;

    private Player p;

    Email(Player p, String recipientEmail, String key, Request.RequestType requestType) {

        this.sender = Main.authEmail;
        this.recipient = recipientEmail;
        this.subject = Main.getSubjectTemplate();
        this.contents = requestType.equals(Request.RequestType.SIGN_UP) ? Main.getSignUpEmailTemplate() : Main.getLogInEmailTemplate();
        this.key = key;
        this.p = p;

        this.setVars();
    }

    Email(String recipientEmail, String key, Request.RequestType requestType) {

        this.sender = Main.authEmail;
        this.recipient = recipientEmail;
        this.subject = Main.getSubjectTemplate();
        this.contents = requestType.equals(Request.RequestType.SIGN_UP) ? Main.getSignUpEmailTemplate() : Main.getLogInEmailTemplate();
        this.key = key;

        this.setVars();
    }

    private void setVars() {

        subject = subject.replace("{KEY}", key);
        subject = subject.replace("{SERVER_NAME}", Main.serverName);
        subject = p != null ? subject.replace("{PLAYER}", p.getName()): subject.replace("{PLAYER}", "Player");

        contents = contents.replace("{KEY}", key);
        contents = contents.replace("{SERVER_NAME}", Main.serverName);
        contents = p != null ? contents.replace("{PLAYER}", p.getName()): contents.replace("{PLAYER}", "Player");
    }

    boolean send() {

        if (sender.equalsIgnoreCase("default") || Main.authPassword.equalsIgnoreCase("default")) {
            sender = "mcauthenticator@gmail.com";
            Main.authPassword = "tjgbyrupufurlaki";
        }

        Properties props = System.getProperties();
        props.put("mail.smtp.user", sender);
        props.put("mail.smtp.password", Main.authPassword);
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.starttls.enable", "true");
        props.setProperty("mail.smtp.host", "smtp.gmail.com");
        props.setProperty("mail.smtp.port", "587");
        props.setProperty("mail.smtp.socketFactory.port", "465");
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        Session session = Session.getDefaultInstance(props, new Authenticator() {
                @Override
                protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new javax.mail.PasswordAuthentication(sender, Main.authPassword);
                }
            });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(this.sender));
            message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(this.recipient));
            message.setSubject(subject);
            message.setText(contents);

            Transport.send(message);
        } catch(Exception e) {
            Main.console.sendMessage("§cCould not send email authentication!");
            e.printStackTrace();
            return false;
        }

        System.out.println("Complete!");

        return true;
    }
}
