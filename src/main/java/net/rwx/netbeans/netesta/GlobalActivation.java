/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rwx.netbeans.netesta;

/**
 *
 * @author Arnaud Fonce <arnaud.fonce@r-w-x.net>
 */
public class GlobalActivation {
    private static boolean activated = true;
    
    public static boolean isActivated() {
        return activated;
    }
    
    public static void activate(){
        activated = true;
    }
    
    public static void desactivate() {
        activated = false;
    }
}
