/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    private static final long INTERVAL_WAIT_COMPILE_ON_SAVE_IN_MS = 500;

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

        if (!isSourceCodeFile()) {
            return;
        }
        
        if (isCompileOnSaveEnabled(project)) {
            try {
                waitCompileOnSave(dataObject);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        getSourceGourpsForJavaSource();
        if (isActionSupportedAndEnabled() && isTestableSource()) {
            performTestSingleAction();
        }
    }

    private boolean isCompileOnSaveEnabled(Project prj) {
        AuxiliaryProperties auxprops = prj.getLookup().lookup(AuxiliaryProperties.class);
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

    private void waitCompileOnSave(DataObject dataObject) throws InterruptedException {
        FileObject sourceFile = dataObject.getPrimaryFile();
        FileObject buildFile = findClassFileFromSourceFile(sourceFile);
        long startTime = System.currentTimeMillis();
        while (buildFile.lastModified().before(sourceFile.lastModified())) {
            Thread.sleep(INTERVAL_WAIT_COMPILE_ON_SAVE_IN_MS);
            if ((System.currentTimeMillis() - startTime) > MAX_WAIT_COMPILE_ON_SAVE_IN_MS) {
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

    private SourceGroup[] getSourceGourpsForJavaSource() {
        return ProjectUtils.getSources(project)
                .getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
    }

    private boolean isActionSupportedAndEnabled() {
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

    private boolean isTestableSource() {
        return true;
    }

    private boolean isSourceCodeFile() {
        SourceGroup[] groups = getSourceGourpsForJavaSource();
        if (groups.length < 1) {
            return false;
        }

        for (SourceGroup group : groups) {
            if (FileUtil.isParentOf(group.getRootFolder(), dataObject.getPrimaryFile())) {
                return true;
            }
        }

        return false;
    }

}
