/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rwx.netbeans.netesta;

import org.openide.modules.OnStart;
import org.openide.windows.WindowManager;

/**
 *
 * @author Arnaud Fonce <arnaud.fonce@r-w-x.net>
 */
@OnStart
public class OnStartNetesta implements Runnable {

    @Override
    public void run() {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                WindowManager.getDefault().getRegistry()
                        .addPropertyChangeListener(new TopComponentChangeListener());
            }
        });

    }
}
