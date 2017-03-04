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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
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

    private WatchService classWatcher, directoryTreeWatcher;
    private RequestProcessor requestProcessor;
    private Project project;

    public CompiledFileObserver(FileChangeListener listener, FileObject compiledFile) {
        this.listener = listener;
        this.compiledFile = compiledFile;
        this.project = FileOwnerQuery.getOwner(compiledFile);
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
        Path path = Paths.get(compiledFile.getPath()).getParent();
        classWatcher = loadWatchService(path, getRunnableToWatchCompiledClassFile(), ENTRY_MODIFY);

        directoryTreeWatcher = loadWatchService(path.getParent(), getRunnableToWatchParentDirectory(), ENTRY_CREATE);
        loadWatchServiceForDirectoryTree(path.getParent().getParent());
    }

    private void loadWatchServiceForDirectoryTree(Path path) throws IOException {
        if (path.startsWith(project.getProjectDirectory().getPath())) {
            path.register(directoryTreeWatcher, ENTRY_CREATE);
            loadWatchServiceForDirectoryTree(path.getParent());
        }
    }

    private WatchService loadWatchService(Path path, WatchKeyConsumer monitor, Kind<?>... events) throws IOException {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        monitor.setWatchService(watcher);
        requestProcessor.execute(monitor);
        path.register(watcher, events);
        return watcher;
    }

    private WatchKeyConsumer getRunnableToWatchCompiledClassFile() {
        return new WatchKeyConsumer() {
            @Override
            public void consumeWatchKey(WatchKey key, WatchEvent event) {
                Path eventPath = ((Path) key.watchable()).resolve(((Path) event.context()));
                if (eventPath.toString().equals(compiledFile.getPath())) {
                    listener.fileChanged(new FileEvent(compiledFile));
                }
            }
        };
    }

    private WatchKeyConsumer getRunnableToWatchParentDirectory() {
        return new WatchKeyConsumer() {
            @Override
            public void consumeWatchKey(WatchKey key, WatchEvent event) throws IOException {
                Path eventPath = ((Path) key.watchable()).resolve(((Path) event.context()));
                restoreDirectoryTreeWatcherForPath(eventPath);
            }
        };
    }

    private void restoreDirectoryTreeWatcherForChildrenPath(Path path) throws IOException {
        if( path.toFile().exists() ) {
            for (File child : path.toFile().listFiles()) {
                Path childPath = Paths.get(child.getPath());
                restoreDirectoryTreeWatcherForPath(childPath);
            }
        }
    }

    private void restoreDirectoryTreeWatcherForPath(Path path) throws IOException {
        Path compiledFileDir = Paths.get(compiledFile.getPath()).getParent();
        if (compiledFileDir.toString().startsWith(path.toString())) {
            if (!compiledFileDir.toString().equals(path.toString())) {
                if( path.toFile().exists() ) {
                    path.register(directoryTreeWatcher, ENTRY_CREATE);
                    restoreDirectoryTreeWatcherForChildrenPath(path);
                }
            } else {
                compiledFileDir.register(classWatcher, ENTRY_MODIFY);
            }
        }
    }

    public void stop() {
        try {
            classWatcher.close();
            directoryTreeWatcher.close();
            requestProcessor.shutdownNow();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
