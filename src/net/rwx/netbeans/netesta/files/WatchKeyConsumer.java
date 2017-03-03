/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rwx.netbeans.netesta.files;

import java.nio.file.ClosedWatchServiceException;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import org.openide.util.Exceptions;

/**
 *
 * @author mlvtito
 */
public abstract class WatchKeyConsumer implements Runnable {

    private WatchService watchService;

    public void setWatchService(WatchService watchService) {
        this.watchService = watchService;
    }

    @Override
    public void run() {
        try {
            WatchKey key = watchService.take();
            while (key != null) {
                consumeWatchKey(key);
                key = watchService.take();
            }
        } catch (ClosedWatchServiceException e) {
            Exceptions.printStackTrace(e);
        } catch (InterruptedException e) {
            Exceptions.printStackTrace(e);
        }
    }

    public abstract void consumeWatchKey(WatchKey key);
}
