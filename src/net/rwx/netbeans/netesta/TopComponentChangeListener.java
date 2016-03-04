/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
public class TopComponentChangeListener implements PropertyChangeListener {

    private static final String PROPERTY_NAME_OPENED = "opened";
    private static final String PROPERTY_NAME_ACTIVATED = "activated";

    private final FileChangeListener fileChangeListener = new OpenFileChangeListener();

    public TopComponentChangeListener() {
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
