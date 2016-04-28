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
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Arnaud Fonce <arnaud.fonce@r-w-x.net>
 */
public class TestSingleRunnable implements Runnable {

    private static final long MAX_WAIT_COMPILE_ON_SAVE_IN_MS = 10000;
    private static final long INTERVAL_WAIT_COMPILE_ON_SAVE_IN_MS = 100;

    private final Project project;
    private final DataObject dataObject;
    private final ActionProvider actionProvider;

    public TestSingleRunnable(DataObject dataObject) {
        this.dataObject = dataObject;
        this.project = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
        if (project != null) {
            this.actionProvider = project.getLookup().lookup(ActionProvider.class);
        } else {
            this.actionProvider = null;
        }
    }

    @Override
    public void run() {
        if (project == null || actionProvider == null) {
            return;
        }

        if (isCompileOnSaveEnabled()) {
            waitCompileOnSave();
        }

        if (isActionSupportedAndEnabled()) {
            performTestSingleAction();
        }
    }

    public boolean isCompileOnSaveEnabled() {
        AuxiliaryProperties auxprops = project.getLookup().lookup(AuxiliaryProperties.class);
        if (auxprops == null) {
            // Cannot use ProjectUtils.getPreferences due to compatibility.
            return false;
        }
        String cos = auxprops.get("netbeans.compile.on.save", true);
        if (cos == null) {
            cos = "all";
        }
        return !"none".equalsIgnoreCase(cos);
    }

    private void waitCompileOnSave() {
        try {
            tryToWaitCompileOnSave();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void tryToWaitCompileOnSave() throws InterruptedException {
        FileObject sourceFile = dataObject.getPrimaryFile();
        FileObject buildFile = findClassFileFromSourceFile(sourceFile);
        long startTime = System.currentTimeMillis();
        while (sourceFileNotCompiled(sourceFile, buildFile)) {
            Thread.sleep(INTERVAL_WAIT_COMPILE_ON_SAVE_IN_MS);
            if ((System.currentTimeMillis() - startTime) > MAX_WAIT_COMPILE_ON_SAVE_IN_MS) {
                return;
            }
        }
    }

    private static boolean sourceFileNotCompiled(FileObject sourceFile, FileObject buildFile) {
        return buildFile == null
                || buildFile.lastModified().before(sourceFile.lastModified());
    }

    private FileObject findClassFileFromSourceFile(FileObject file) {
        ClassPath sourceClassPath = ClassPath.getClassPath(file, ClassPath.SOURCE);
        ClassPath cp = ClassPath.getClassPath(file, ClassPath.EXECUTE);
        return cp.findResource(
                sourceClassPath.getResourceName(file, '/', false) + ".class");
    }

    public boolean isActionSupportedAndEnabled() {
        String actionCode = ActionProvider.COMMAND_TEST_SINGLE;
        return isTestFileActionIn(actionProvider.getSupportedActions())
                && actionProvider.isActionEnabled(actionCode, Lookups.fixed(dataObject));
    }

    private void performTestSingleAction() {
        actionProvider.invokeAction(
                ActionProvider.COMMAND_TEST_SINGLE,
                Lookups.fixed(dataObject)
        );
    }

    private boolean isTestFileActionIn(String[] actions) {
        for (String action : actions) {
            if (action.equals(ActionProvider.COMMAND_TEST_SINGLE)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasTestClass() {
        SourceGroup group = getSourceGroup();
        System.out.println("net.rwx.netbeans.netesta.TestSingleRunnable.hasTestClass() : " + group.getName());
        // 1SourceRoot
        // 2TestSourceRoot
        return true;
    }

    private SourceGroup getSourceGroup() {
        SourceGroup[] groups = ProjectUtils.getSources(project)
                .getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        for( SourceGroup group : groups ) {
            if (FileUtil.isParentOf(group.getRootFolder(), dataObject.getPrimaryFile())) {
                return group;
            }
        }
        
        throw new RuntimeException("Unable to find parent group for " + dataObject.getPrimaryFile());
    }
}
