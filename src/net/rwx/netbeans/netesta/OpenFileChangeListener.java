/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rwx.netbeans.netesta;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 *
 * @author Arnaud Fonce <arnaud.fonce@r-w-x.net>
 */
public class OpenFileChangeListener extends FileChangeAdapter {

    private static final long MAX_WAIT_COMPILE_ON_SAVE_IN_MS = 5000;
    private static final long INTERVAL_WAIT_COMPILE_ON_SAVE_IN_MS = 500;

    @Override
    public void fileChanged(FileEvent fe) {
        ProgressHandle ph = null;
        try {
            DataObject dataObject = DataObject.find(fe.getFile());
            ph = ProgressHandle.createHandle("Wait to test (" + dataObject.getName() + ")");
            ph.start();

            waitCompileOnSave(dataObject);
            
            TestSingleRunnable testSingle = new TestSingleRunnable(dataObject);
            testSingle.run();
            
        } catch (DataObjectNotFoundException | InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }finally {
            if( ph != null ) {
                ph.finish();
            }
        }
    }

    private void waitCompileOnSave(DataObject dataObject) throws InterruptedException {
        FileObject sourceFile = dataObject.getPrimaryFile();
        FileObject buildFile = findClassFileFromSourceFile(sourceFile);
        long startTime = System.currentTimeMillis();
        while(buildFile.lastModified().before(sourceFile.lastModified())) {
            Thread.sleep(INTERVAL_WAIT_COMPILE_ON_SAVE_IN_MS);
            if( (System.currentTimeMillis() - startTime) > MAX_WAIT_COMPILE_ON_SAVE_IN_MS ) {
                return;
            }
        }
    }

    private FileObject findClassFileFromSourceFile(FileObject file) {
        ClassPath sourceClassPath = ClassPath.getClassPath(file, ClassPath.SOURCE);
        ClassPath cp = ClassPath.getClassPath(file, ClassPath.EXECUTE);
        return cp.findResource(
                sourceClassPath.getResourceName(file, '/', false) + ".class");
    }
}