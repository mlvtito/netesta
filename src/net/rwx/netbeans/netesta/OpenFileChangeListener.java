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

    @Override
    public void fileChanged(FileEvent fe) {
        ProgressHandle ph = null;
        try {
            DataObject dataObject = DataObject.find(fe.getFile());
            ph = ProgressHandle.createHandle("Wait to test (" + dataObject.getName() + ")");
            ph.start();

            TestSingleRunnable testSingle = new TestSingleRunnable(dataObject);
            testSingle.run();

        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (ph != null) {
                ph.finish();
            }
        }
    }
}
