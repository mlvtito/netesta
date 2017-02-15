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
package net.rwx.netbeans.netesta.ui;

import org.netbeans.api.progress.ProgressHandle;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Arnaud Fonce <arnaud.fonce@r-w-x.net>
 */
public class Progressor implements AutoCloseable {

    private final FileObject fileObject;
    private ProgressHandle progressHandle;

    public Progressor(FileObject fileObject) {
        this.fileObject = fileObject;
    }
    
    public void start() {
        progressHandle = ProgressHandle.createHandle(
                "Wait to test (" + fileObject.getName() + ")"
        );
        progressHandle.start();
    }
    
    @Override
    public void close() {
        if( progressHandle != null ) {
            progressHandle.finish();
        }
    }
}
