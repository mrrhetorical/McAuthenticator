package com.rhetorical.auth;

import com.rhetorical.auth.signing.SignInState;
import org.bukkit.entity.Player;

public class AuthPlayer {

    public Player parent;
    private boolean authenticated;
    private SignInState authState;

    public AuthPlayer(Player p) {
        this.parent = p;
        this.authState = SignInState.AWAITING_USERNAME;
        this.authenticated = false;
    }



    public boolean hasPerm(String permission) {
        if (parent.hasPermission(permission) || parent.hasPermission("McAuth.*") || parent.isOp())
            return true;

        return false;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void authenticate() {
        this.authenticated = true;
        this.authState = SignInState.COMPLETE;
    }

    public SignInState getAuthState() {
        return this.authState;
    }

    public void proceedToTwoFactor() {
        this.authState = SignInState.AWAITING_2FA;
    }

    public void proceedToPassword() {
        this.authState = SignInState.AWAITING_PASSWORD;
    }

}
