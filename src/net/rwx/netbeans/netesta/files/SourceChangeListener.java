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
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 *
 * @author Arnaud Fonce <arnaud.fonce@r-w-x.net>
 */
public class SourceChangeListener extends FileChangeAdapter {

    private final TestActionFactory operationFactory;

    public SourceChangeListener() {
        this.operationFactory = TestActionFactory.get();
    }

    @Override
    public void fileChanged(FileEvent fe) {
        try {
            DataObject dataObject = DataObject.find(fe.getFile());
            TestAction testOperation = operationFactory.get(dataObject);

            if (testOperation.isActionSupportedAndEnabled() && testOperation.hasTestClass()) {
                if (testOperation.isCompileOnSaveEnabled() && !testOperation.isWaitingForCompilation()) {
                    testOperation.waitForCompilation();
                } else if (!testOperation.isCompileOnSaveEnabled()) {
                    testOperation.run();
                }
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
