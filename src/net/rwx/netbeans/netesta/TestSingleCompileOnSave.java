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

import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Arnaud Fonce <arnaud.fonce@r-w-x.net>
 */
public class TestSingleCompileOnSave {
    private static final long MAX_WAIT_COMPILE_ON_SAVE_IN_MS = 60000;
    private static final long INTERVAL_WAIT_COMPILE_ON_SAVE_IN_MS = 100;
    
    private final DataObject dataObject;
    private boolean waitingForCompilation = false;
    
    public TestSingleCompileOnSave(DataObject dataObject) {
        this.dataObject = dataObject;
    }
    
    public void waitForCompilation() {
        try {
            waitingForCompilation = true;
            tryToWaitCompileOnSave();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }finally {
            waitingForCompilation = false;
        }
    }

    public boolean isWaitingForCompilation() {
        return waitingForCompilation;
    }

    public void resetWaiting() {
        System.out.println("################ RESETING TIMEOUT" );
        startTime = System.currentTimeMillis();
    }
    
    private long startTime;
    private void tryToWaitCompileOnSave() throws InterruptedException {
        FileObject sourceFile = dataObject.getPrimaryFile();
        FileObject buildFile = findClassFileFromSourceFile(sourceFile);
        startTime = System.currentTimeMillis();
        while (sourceFileNotCompiled(sourceFile, buildFile)) {
            Thread.sleep(INTERVAL_WAIT_COMPILE_ON_SAVE_IN_MS);
            if ((System.currentTimeMillis() - startTime) > MAX_WAIT_COMPILE_ON_SAVE_IN_MS) {
                System.out.println("################ TIMEOUTING # " + buildFile.isLocked() );
                return;
            }
            buildFile = findClassFileFromSourceFile(sourceFile);
            buildFile.refresh();
        }
    }
    
    private FileObject findClassFileFromSourceFile(FileObject file) {
        ClassPath sourceClassPath = ClassPath.getClassPath(file, ClassPath.SOURCE);
        ClassPath cp = ClassPath.getClassPath(file, ClassPath.EXECUTE);
        return cp.findResource(
                sourceClassPath.getResourceName(file, '/', false) + ".class");
    }
    
    private static boolean sourceFileNotCompiled(FileObject sourceFile, FileObject buildFile) {
        System.out.println("#######################################################################################");
        System.out.println("############### sourceFile  : (" + sourceFile.lastModified() + ")" + sourceFile);
        System.out.println("############### buildFile   : (" + buildFile.lastModified() + ")" + buildFile);
        System.out.println("#######################################################################################");
        return buildFile == null
                || sourceFile.lastModified().after(buildFile.lastModified());
    }
}
