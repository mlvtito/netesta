/*
 * Copyright 2017 Arnaud Fonce <arnaud.fonce@r-w-x.net>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.rwx.netbeans.netesta.files;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import org.openide.util.Exceptions;

/**
 *
 * @author Arnaud Fonce <arnaud.fonce@r-w-x.net>
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
                for (WatchEvent event : key.pollEvents()) {
                    try {
                        consumeWatchKey(key, event);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                key.reset();
                key = watchService.take();
            }
        } catch (ClosedWatchServiceException e) {
//            Exceptions.printStackTrace(e);
        } catch (InterruptedException e) {
            Exceptions.printStackTrace(e);
        }
    }

    public abstract void consumeWatchKey(WatchKey key, WatchEvent event) throws IOException;
}
