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

import net.rwx.netbeans.netesta.files.SourceChangeListener;
import net.rwx.netbeans.netesta.files.CompiledFileChangeListener;
import net.rwx.netbeans.netesta.files.CompiledFileObserver;
import net.rwx.netbeans.netesta.action.TestAction;
import net.rwx.netbeans.netesta.action.TestActionFactory;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Arnaud Fonce <arnaud.fonce@r-w-x.net>
 */
public class NetestaHandler {

    private final FileChangeListener sourceChangeListener = new SourceChangeListener();
    private final CompiledFileObserver observer;

    private final FileObject source;

    public NetestaHandler(DataObject dataObject) {
        this.source = dataObject.getPrimaryFile();
        observer = new CompiledFileObserver(dataObject);
    }

    public void init() {
        source.addFileChangeListener(sourceChangeListener);
        observer.start();
    }

    public void release() {
        source.removeFileChangeListener(sourceChangeListener);
        observer.stop();
    }
}
