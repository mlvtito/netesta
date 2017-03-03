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
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.UUID;
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

    private WatchService classWatcher, parentWatcher;
    private RequestProcessor requestProcessor;

    public CompiledFileObserver(FileChangeListener listener, FileObject compiledFile) {
        this.listener = listener;
        this.compiledFile = compiledFile;
    }

    public void start() {
        try {
            requestProcessor = new RequestProcessor("netesta-compiled-observer-" + compiledFile.getName(), 2, true);
            loadWatchServices();
            
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void loadWatchServices() throws IOException {
        Path path = Paths.get(compiledFile.getParent().getPath());
        classWatcher = loadWatchService(path, ENTRY_MODIFY, getRunnableToWatchCompiledClassFile());
        parentWatcher = loadWatchService(path.getParent(), ENTRY_DELETE, getRunnableToWatchParentDirectory());
    }

    private WatchService loadWatchService(Path path, Kind<?> event, WatchKeyConsumer monitor) throws IOException {
        WatchService watcher = path.getFileSystem().newWatchService();
        monitor.setWatchService(watcher);
        requestProcessor.execute(monitor);
        path.register(watcher, event);
        return watcher;
    }
    
    private WatchKeyConsumer getRunnableToWatchCompiledClassFile() {
        return new WatchKeyConsumer() {
            @Override
            public void consumeWatchKey(WatchKey key) {
                String uuid = UUID.randomUUID().toString();
                for (WatchEvent event : key.pollEvents()) {
                    System.out.println("#### CLASS #####" + uuid + "######## " + event.kind() + " ############### " + (Path) event.context());
                    Path path = (Path) event.context();
                    Path compiledPath = Paths.get(compiledFile.getPath()).getFileName();
                    if (path.compareTo(compiledPath) == 0) {
                        listener.fileChanged(new FileEvent(compiledFile));
                    }
                }
                key.reset();
            }
        };
    }

    private WatchKeyConsumer getRunnableToWatchParentDirectory() {
        return new WatchKeyConsumer() {
            @Override
            public void consumeWatchKey(WatchKey key) {
                String uuid = UUID.randomUUID().toString();
                for (WatchEvent event : key.pollEvents()) {
                    System.out.println("#### PARENT #####" + uuid + "######## " + event.kind() + " ############### " + (Path) event.context());
                     Path path = (Path) event.context();
                     Path expectedPath = Paths.get(compiledFile.getPath()).getParent().getFileName();
                     if (path.compareTo(expectedPath) == 0) {
                         System.out.println("#################### SHOULD RELOAD WATCHERS");
                         try {
                         closeWatchers();
                         // wait directory to exist. What happen if build failed ?
//                         loadWatchServices();
                         }catch(IOException ioe) {
                             throw new RuntimeException(ioe);
                         }
                    }
                }
                key.reset();
            }
        };
    }

    private void closeWatchers() throws IOException {
            classWatcher.close();
            parentWatcher.close();
    }
    
    public void stop() {
        try {
            classWatcher.close();
            parentWatcher.close();
            requestProcessor.shutdownNow();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
