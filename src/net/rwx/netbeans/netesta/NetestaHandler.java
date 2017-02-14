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
package net.rwx.netbeans.netesta;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.Executors;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Arnaud Fonce <arnaud.fonce@r-w-x.net>
 */
public class NetestaHandler {

    private final FileChangeListener sourceChangeListener = new SourceChangeListener();
    private final CompiledFileObserver observer;

    private final FileObject source;
    private final FileObject compiled;

    public NetestaHandler(DataObject dataObject) {
        this.source = dataObject.getPrimaryFile();
        this.compiled = findClassFileFromSourceFile(source);
        TestOperation testOperation = TestOperationFactory.get().get(dataObject);
        FileChangeListener compiledChangeListener = new CompiledFileChangeListener(testOperation);
        observer = new CompiledFileObserver(compiledChangeListener, compiled);
    }

    public void init() {
        System.out.println("### ## ADDING SOURCE LISTENER ON " + source);
        source.addFileChangeListener(sourceChangeListener);
        System.out.println("### ## STARTING COMPILED OBSERVER ON " + compiled);
        observer.start();
    }

    public void release() {
        source.removeFileChangeListener(sourceChangeListener);
        // observer.stop();
    }

    private FileObject findClassFileFromSourceFile(FileObject file) {
        ClassPath sourceClassPath = ClassPath.getClassPath(file, ClassPath.SOURCE);
        ClassPath cp = ClassPath.getClassPath(file, ClassPath.EXECUTE);
        return cp.findResource(
                sourceClassPath.getResourceName(file, '/', false) + ".class");
    }
}
