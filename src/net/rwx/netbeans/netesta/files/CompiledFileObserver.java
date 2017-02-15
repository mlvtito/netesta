/*
 * Copyright 2016 Arnaud Fonce <arnaud.fonce@r-w-x.net>.
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Arnaud Fonce <arnaud.fonce@r-w-x.net>
 */
public class CompiledFileObserver {

    private final FileChangeListener listener;
    private final FileObject compiledFile;

    private WatchService watchService;
    private RequestProcessor requestProcessor;
    
    public CompiledFileObserver(FileChangeListener listener, FileObject compiledFile) {
        this.listener = listener;
        this.compiledFile = compiledFile;
    }

    public void start() {
        try {
            Path pathToMonitor = Paths.get(compiledFile.getParent().getPath());
            watchService = Paths.get(compiledFile.getParent().getPath()).getFileSystem().newWatchService();
            requestProcessor = new RequestProcessor("netesta-compiled-observer-" + compiledFile.getName());
            requestProcessor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        WatchKey key = watchService.take();
                        while (key != null) {
                            consumeWatchKey(key);
                            key = watchService.take();
                        }
                    } catch( ClosedWatchServiceException e) {
                        
                    } catch (InterruptedException e) {
                        Exceptions.printStackTrace(e);
                    }
                }

                private void consumeWatchKey(WatchKey key) {
                    for (WatchEvent event : key.pollEvents()) {
                        Path path = (Path) event.context();
                        Path compiledPath = Paths.get(compiledFile.getPath()).getFileName();
                        if (path.compareTo(compiledPath) == 0) {
                            listener.fileChanged(new FileEvent(compiledFile));
                        }
                    }
                    key.reset();
                }
            });
            pathToMonitor.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void stop() {
        try {
            watchService.close();
            requestProcessor.shutdownNow();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
