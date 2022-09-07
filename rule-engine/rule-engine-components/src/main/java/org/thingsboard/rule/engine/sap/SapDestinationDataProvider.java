/**
 * Copyright © 2016-2022 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.rule.engine.sap;

import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SapDestinationDataProvider implements DestinationDataProvider {

    private static final SapDestinationDataProvider INSTANCE = new SapDestinationDataProvider();

    private DestinationDataEventListener destinationDataEventListener;
    private Map<String, Properties> destinationPropertiesMap = new HashMap<>();

    private SapDestinationDataProvider() {
    }

    public static SapDestinationDataProvider getInstance() {
        return INSTANCE;
    }

    public void registerDestination(@NotNull final String destinationName, @NotNull final Properties destinationProperties) {
        this.destinationPropertiesMap.put(destinationName, destinationProperties);

        if (null != this.destinationDataEventListener) {
            this.destinationDataEventListener.updated(destinationName);
        }
    }

    public void unregisterDestination(@NotNull final String destinationName) {
        this.destinationPropertiesMap.remove(destinationName);

        if (null != this.destinationDataEventListener) {
            this.destinationDataEventListener.deleted(destinationName);
        }
    }

    @Override
    public Properties getDestinationProperties(final String destinationName) {
        return this.destinationPropertiesMap.get(destinationName);
    }

    @Override
    public boolean supportsEvents() {
        return true;
    }

    @Override
    public void setDestinationDataEventListener(DestinationDataEventListener destinationDataEventListener) {
        this.destinationDataEventListener = destinationDataEventListener;
    }
}
