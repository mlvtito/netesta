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
package net.rwx.netbeans.netesta.files;

import net.rwx.netbeans.netesta.action.TestAction;
import net.rwx.netbeans.netesta.action.TestActionFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.AuxiliaryProperties;
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
public class SourceChangeListener extends FileChangeAdapter {

    @Override
    public void fileChanged(FileEvent fe) {
        try {
            DataObject dataObject = DataObject.find(fe.getFile());
            TestAction testAction = TestActionFactory.get().get(dataObject);

            if (testAction.supportedAndEnabled() && testAction.hasNeededSourceTestClass()) {
                if (isCompileOnSaveEnabled(fe.getFile()) && !testAction.isWaitingForCompilation()) {
                    testAction.waitForCompilation();
                } else if (!isCompileOnSaveEnabled(fe.getFile())) {
                    testAction.run();
                }
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private boolean isCompileOnSaveEnabled(FileObject fileObject) {
        Project project = FileOwnerQuery.getOwner(fileObject);
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
}
