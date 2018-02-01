/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rwx.netbeans.netesta.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import net.rwx.netbeans.netesta.GlobalActivation;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author Arnaud Fonce <arnaud.fonce@r-w-x.net>
 */
@ActionID(category = "File", id = "net.rwx.netbeans.netesta.GlobalActionvationListener")
@ActionRegistration(displayName = "#netesta.disable")
@ActionReference(path = "UI/ToolActions/Files", position = 0)
public class GlobalActionvationListener extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        if (GlobalActivation.isActivated()) {
            putValue("menuText", NbBundle.getMessage(GlobalActionvationListener.class, "netesta.enable"));
            GlobalActivation.desactivate();
        } else {
            putValue("menuText", NbBundle.getMessage(GlobalActionvationListener.class, "netesta.disable"));
            GlobalActivation.activate();
        }
    }
}
