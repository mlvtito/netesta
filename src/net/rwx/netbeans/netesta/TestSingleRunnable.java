/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rwx.netbeans.netesta;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ActionProvider;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Arnaud Fonce <arnaud.fonce@r-w-x.net>
 */
public class TestSingleRunnable implements Runnable {

    private final Project project;
    private final DataObject dataObject;
    private final ActionProvider actionProvider;

    public TestSingleRunnable(DataObject dataObject) {
        this.dataObject = dataObject;
        this.project = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
        if (project != null) {
            this.actionProvider = project.getLookup().lookup(ActionProvider.class);
        }else {
            this.actionProvider = null;
        }
    }

    @Override
    public void run() {
        if (project == null || actionProvider == null) {
            return;
        }

        getSourceGourpsForJavaSource();
        if (isActionSupportedAndEnabled() && isTestableSource()) {
            performTestSingleAction();
        }
    }

    private void getSourceGourpsForJavaSource() {
        SourceGroup[] groups = ProjectUtils
                .getSources(project)
                .getSourceGroups(
                        JavaProjectConstants.SOURCES_TYPE_JAVA
                );
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

}
