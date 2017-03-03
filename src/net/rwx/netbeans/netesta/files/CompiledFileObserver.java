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
import java.util.UUID;
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

    private WatchService classWatcher, parentWatcher;
    private RequestProcessor requestProcessor;
    private Project project;

    public CompiledFileObserver(FileChangeListener listener, FileObject compiledFile) {
        this.listener = listener;
        this.compiledFile = compiledFile;
        this.project = FileOwnerQuery.getOwner(compiledFile);
        System.out.println("############### " + project.getProjectDirectory());
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
        classWatcher = loadWatchService(path, getRunnableToWatchCompiledClassFile(), ENTRY_MODIFY);

        parentWatcher = loadWatchService(path.getParent(), getRunnableToWatchParentDirectory(), ENTRY_DELETE, ENTRY_CREATE);
        loadWatchServiceForDirectoryTree(path.getParent().getParent());
    }

    private void loadWatchServiceForDirectoryTree(Path path) throws IOException {
        System.out.println("### Should I set watcher on path " + path + " for project " + project.getProjectDirectory().getPath());
        if (path.toString().startsWith(project.getProjectDirectory().getPath())) {
            System.out.println("Setting watcher !!!!!!!");
            path.register(parentWatcher, ENTRY_DELETE, ENTRY_CREATE);
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
                    try {
                        Path eventPath = ((Path)key.watchable()).resolve(((Path)event.context()));
                        System.out.println("#### PARENT #####" + uuid + "######## " + event.kind() + " ############### " + eventPath.toString() + " ## " + compiledFile.getPath());
                        if (event.kind() == ENTRY_DELETE) {
//                            classWatcher.close();
                        } else if (event.kind() == ENTRY_CREATE) {
                            if (compiledFile.getPath().startsWith(eventPath.toString())) {
                                System.out.println("#### PARENT ##### Resetting watcher " + eventPath);
                                eventPath.register(parentWatcher, ENTRY_DELETE, ENTRY_CREATE);
                            }
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
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

    private boolean isParentOf(Path parent, Path child) {
        Path iteratePath = child.getParent();
        while (iteratePath != null) {
            if (parent.toString().equals(iteratePath.toString())) {
                return true;
            }
            iteratePath = iteratePath.getParent();
        }
        return false;
    }
}
