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
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import net.rwx.netbeans.netesta.action.TestAction;
import net.rwx.netbeans.netesta.action.TestActionFactory;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Arnaud Fonce <arnaud.fonce@r-w-x.net>
 */
public class CompiledFileObserver {

    private final FileChangeListener listener;
    private final File compiledFile;

    private WatchService classWatcher, directoryTreeWatcher;
    private RequestProcessor requestProcessor;
    private final Project project;

    public CompiledFileObserver(DataObject sourceCode) {
        TestAction testOperation = TestActionFactory.get().get(sourceCode);
        this.listener = new CompiledFileChangeListener(testOperation);
        this.compiledFile = findClassFileFromSourceFile(sourceCode.getPrimaryFile());
        this.project = FileOwnerQuery.getOwner(sourceCode.getPrimaryFile());
    }

    private File findClassFileFromSourceFile(FileObject file) {
        ClassPath sourceClassPath = ClassPath.getClassPath(file, ClassPath.SOURCE);
        ClassPath cp = ClassPath.getClassPath(file, ClassPath.EXECUTE);
        String classFile = cp.entries().get(0).getURL().getPath() + sourceClassPath.getResourceName(file, '/', false) + ".class";
        return new File(classFile);
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
        classWatcher = buildWatchService(actionForCompiledClassWatcher());
        directoryTreeWatcher = buildWatchService(actionForDirectoryTreeWatcher());
        restoreDirectoryTreeWatcherForPath(Paths.get(project.getProjectDirectory().getPath()));
    }

    private WatchService buildWatchService(WatchKeyConsumer monitor) throws IOException {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        monitor.setWatchService(watcher);
        requestProcessor.execute(monitor);
        return watcher;
    }

    private WatchKeyConsumer actionForCompiledClassWatcher() {
        return new WatchKeyConsumer() {
            @Override
            public void consumeWatchKey(WatchKey key, WatchEvent event) {
                Path eventPath = ((Path) key.watchable()).resolve(((Path) event.context()));
                Path compiledPath = Paths.get(compiledFile.getPath());
                if (eventPath.equals(compiledPath)) {
                    listener.fileChanged(new FileEvent(FileUtil.toFileObject(compiledFile)));
                }
            }
        };
    }

    private WatchKeyConsumer actionForDirectoryTreeWatcher() {
        return new WatchKeyConsumer() {
            @Override
            public void consumeWatchKey(WatchKey key, WatchEvent event) throws IOException {
                Path eventPath = ((Path) key.watchable()).resolve(((Path) event.context()));
                restoreDirectoryTreeWatcherForPath(eventPath);
            }
        };
    }

    private void restoreDirectoryTreeWatcherForChildrenPath(Path path) throws IOException {
        if (path.toFile().exists()) {
            for (File child : path.toFile().listFiles()) {
                Path childPath = Paths.get(child.getPath());
                restoreDirectoryTreeWatcherForPath(childPath);
            }
        }
    }

    private void restoreDirectoryTreeWatcherForPath(Path path) throws IOException {
        Path classDir = Paths.get(compiledFile.getPath()).getParent();
        if (classDir.startsWith(path)) {
            if (!classDir.equals(path)) {
                if (path.toFile().exists()) {
                    path.register(directoryTreeWatcher, ENTRY_CREATE);
                    restoreDirectoryTreeWatcherForChildrenPath(path);
                }
            } else {
                classDir.register(classWatcher, ENTRY_MODIFY);
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
