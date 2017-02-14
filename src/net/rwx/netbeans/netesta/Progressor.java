/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rwx.netbeans.netesta;

import org.netbeans.api.progress.ProgressHandle;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mlvtito
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
