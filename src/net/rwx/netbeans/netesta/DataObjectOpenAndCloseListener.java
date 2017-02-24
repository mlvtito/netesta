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

import net.rwx.netbeans.netesta.files.SourceChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;

/**
 *
 * @author Arnaud Fonce <arnaud.fonce@r-w-x.net>
 */
public class DataObjectOpenAndCloseListener implements PropertyChangeListener {

    private static final String PROPERTY_NAME_OPENED = "opened";

    public DataObjectOpenAndCloseListener() {
        addChangeListenerOnEveryOpenedTestableSourceCode();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (openedTopComponent(event)) {
            List<DataObject> newOpenedDataObject = findNewlyOpenedDataObject(event);
            addChangeListenerIfTestableSourceCode(newOpenedDataObject);

            List<DataObject> newClosedDataObject = findNewlyClosedDataObject(event);
            removeChangeListener(newClosedDataObject);
        }
    }

    private void addChangeListenerOnEveryOpenedTestableSourceCode() {
        for (TopComponent topComponent : TopComponent.getRegistry().getOpened()) {
            DataObject dataObject = topComponent.getLookup().lookup(DataObject.class);
            if (dataObject != null) {
                addChangeListenerIfTestableSourceCode(dataObject);
            }
        }
    }

    private void addChangeListenerIfTestableSourceCode(List<DataObject> dataObjects) {
        addChangeListenerIfTestableSourceCode(dataObjects.toArray(new DataObject[dataObjects.size()]));
    }

    private Map<DataObject, NetestaHandler> handlers = new HashMap<>();
    
    private void addChangeListenerIfTestableSourceCode(DataObject... dataObjects) {
        for (DataObject dataObject : dataObjects) {
            if (dataObject != null && isTestableSourceCode(dataObject)) {
                handlers.put(dataObject, new NetestaHandler(dataObject));
                handlers.get(dataObject).init();
            }
        }
    }

    private void removeChangeListener(List<DataObject> dataObjects) {
        for (DataObject dataObject : dataObjects) {
            if (dataObject != null && handlers.containsKey(dataObject)) {
                handlers.remove(dataObject).release();
            }
        }
    }

    private boolean isTestableSourceCode(DataObject dataObject) {
        SourceGroup[] groups = getSourceGroupsForJavaSource(dataObject);
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

    private SourceGroup[] getSourceGroupsForJavaSource(DataObject dataObject) {
        Project project = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
        if (project != null) {
            return ProjectUtils.getSources(project)
                    .getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        } else {
            return new SourceGroup[0];
        }
    }

    private static boolean openedTopComponent(PropertyChangeEvent event) {
        return event.getPropertyName().equals(PROPERTY_NAME_OPENED);
    }

    private List<DataObject> findNewlyOpenedDataObject(PropertyChangeEvent event) {
        Set<TopComponent> newSet = (Set<TopComponent>) event.getNewValue();
        Set<TopComponent> oldSet = (Set<TopComponent>) event.getOldValue();
        return getDataObjects().fromTopComponents(newSet).notIn(oldSet);
    }

    private List<DataObject> findNewlyClosedDataObject(PropertyChangeEvent event) {
        Set<TopComponent> newSet = (Set<TopComponent>) event.getNewValue();
        Set<TopComponent> oldSet = (Set<TopComponent>) event.getOldValue();
        return getDataObjects().fromTopComponents(oldSet).notIn(newSet);
    }

    private DataObjectSet getDataObjects() {
        return new DataObjectSet();
    }

    private class DataObjectSet {

        private Set<TopComponent> topComponents;
        private final List<DataObject> dataObjects = new ArrayList<>();

        public DataObjectSet fromTopComponents(Set<TopComponent> topComponents) {
            this.topComponents = topComponents;
            return this;
        }

        public List<DataObject> notIn(Set<TopComponent> otherTopComponents) {
            for (TopComponent topComponent : topComponents) {
                if (!otherTopComponents.contains(topComponent)) {
                    addDataObjectFromTopComponent(topComponent);
                }
            }
            return dataObjects;
        }

        private void addDataObjectFromTopComponent(TopComponent topComponent) {
            DataObject dataObject = topComponent.getLookup().lookup(DataObject.class);
            if (dataObject != null) {
                dataObjects.add(dataObject);
            }
        }
    }
}
