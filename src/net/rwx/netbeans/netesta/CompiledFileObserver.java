/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author mlvtito
 */
public class CompiledFileObserver {
    
    private final FileChangeListener listener;
    private final FileObject compiledFile;

    public CompiledFileObserver(FileChangeListener listener, FileObject compiledFile) {
        this.listener = listener;
        this.compiledFile = compiledFile;
    }
    
    public void start() {
        try {
            Path path = Paths.get(compiledFile.getParent().getPath());
            final WatchService myWatcher = path.getFileSystem().newWatchService();
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        WatchKey key = myWatcher.take();
                        while (key != null) {
                            for (WatchEvent event : key.pollEvents()) {
                                Path path = (Path) event.context();
                                Path compiledPath = Paths.get(compiledFile.getPath()).getFileName();
                                if( path.compareTo(compiledPath) == 0) {
                                    listener.fileChanged(new FileEvent(compiledFile));
                                }
                            }
                            key.reset();
                            key = myWatcher.take();
                        }
                    } catch (InterruptedException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            });
            path.register(myWatcher, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
