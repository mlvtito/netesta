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

import java.util.HashMap;
import java.util.Map;
import org.openide.loaders.DataObject;

/**
 *
 * @author Arnaud Fonce <arnaud.fonce@r-w-x.net>
 */
public class TestOperationFactory {
    
    private static final TestOperationFactory instance = new TestOperationFactory();
    
    private final Map<DataObject, TestOperation> operationCache;

    private TestOperationFactory() {
        operationCache = new HashMap<>();
    }
    
    static TestOperationFactory get() {
        return instance;
    }
    
    void initialize(DataObject dataObject) {
        if( ! operationCache.containsKey(dataObject)) {
            operationCache.put(dataObject, new TestOperation(dataObject));
        }
    }
    
    TestOperation get(DataObject dataObject) {
        initialize(dataObject);
        return operationCache.get(dataObject);
    }
}
