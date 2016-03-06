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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.openide.filesystems.FileChangeListener;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;

/**
 *
 * @author Arnaud Fonce <arnaud.fonce@r-w-x.net>
 */
public class DataObjectOpenAndCloseListener implements PropertyChangeListener {

    private static final String PROPERTY_NAME_OPENED = "opened";

    private final FileChangeListener fileChangeListener = new DataObjectChangeListener();

    public DataObjectOpenAndCloseListener() {
        for (TopComponent topComponent : TopComponent.getRegistry().getOpened()) {
            DataObject dataObject = topComponent.getLookup().lookup(DataObject.class);
            if (dataObject != null) {
                dataObject.getPrimaryFile().addFileChangeListener(fileChangeListener);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (openedTopComponent(event)) {
            List<DataObject> newOpenedDataObject = findNewlyOpenedDataObject(event);
            for (DataObject newOpened : newOpenedDataObject) {
                newOpened.getPrimaryFile().addFileChangeListener(fileChangeListener);
            }

            List<DataObject> newClosedDataObject = findNewlyClosedDataObject(event);
            for (DataObject newClosed : newClosedDataObject) {
                newClosed.getPrimaryFile().removeFileChangeListener(fileChangeListener);
            }
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
