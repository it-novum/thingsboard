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

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.rule.engine.api.*;
import org.thingsboard.rule.engine.api.util.TbNodeUtils;
import org.thingsboard.server.common.data.plugin.ComponentType;
import org.thingsboard.server.common.msg.TbMsg;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Slf4j
@RuleNode(
        type = ComponentType.EXTERNAL,
        name = "SAP Test",
        configClazz = TbSapNodeConfiguration.class,
        nodeDescription = "...",
        nodeDetails = "...",
        uiResources = {"static/rulenode/rulenode-core-config.js"},
        configDirective = "tbActionNodeSapConfig")
public class TbSapNode implements TbNode {

    private TbSapNodeConfiguration config;

    @Override public void init(TbContext ctx, TbNodeConfiguration configuration) throws TbNodeException {
        this.config = TbNodeUtils.convert(configuration, TbSapNodeConfiguration.class);
        final Properties connectProperties = new Properties();

        connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, this.config.getHostName());
        connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, this.config.getSystemNumber());
        connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, this.config.getClient());
        connectProperties.setProperty(DestinationDataProvider.JCO_USER, this.config.getUserName());
        connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, this.config.getPassword());
        connectProperties.setProperty(DestinationDataProvider.JCO_LANG, this.config.getLanguage());

        SapDestinationDataProvider.getInstance().registerDestination("SAP_TEST", connectProperties);

        if (!Environment.isDestinationDataProviderRegistered()) {
            Environment.registerDestinationDataProvider(SapDestinationDataProvider.getInstance());
        }
    }

    @Override public void onMsg(TbContext ctx, TbMsg msg) throws ExecutionException, InterruptedException, TbNodeException {
        try {
            JCoDestination jCoDestination = JCoDestinationManager.getDestination("SAP_TEST");
            jCoDestination.ping();

            final JCoFunction jCoFunction = jCoDestination.getRepository().getFunction("STFC_CONNECTION");
            if (null == jCoFunction) {
                throw new JCoException(JCoException.JCO_ERROR_FUNCTION_NOT_FOUND, "STFC_CONNECTION not found in SAP.");
            }

            jCoFunction.getImportParameterList().setValue("REQUTEXT", "TEST");
            jCoFunction.execute(jCoDestination);

            log.info("STFC_CONNECTION finished:");
            log.info(" Echo: " + jCoFunction.getExportParameterList().getString("ECHOTEXT"));
            log.info(" Response: " + jCoFunction.getExportParameterList().getString("RESPTEXT"));
        } catch (JCoException e) {
            e.printStackTrace();
            log.info("Execution on destination " + "SAP_TEST" + " failed");
        }
    }

    @Override public void destroy() {

    }
}
