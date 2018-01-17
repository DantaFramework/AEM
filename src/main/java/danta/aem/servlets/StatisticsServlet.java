/**
 * Danta AEM Bundle
 *
 * Copyright (C) 2017 Tikal Technologies, Inc. All rights reserved.
 *
 * Licensed under GNU Affero General Public License, Version v3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied;
 * without even the implied warranty of MERCHANTABILITY.
 * See the License for more details.
 */

package danta.aem.servlets;

import danta.aem.services.ContentModelFactoryService;
import danta.api.ContextProcessorEngine;
import danta.api.TemplateContentModel;
import danta.api.configuration.ConfigurationProvider;
import danta.core.execution.ExecutionContextImpl;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static danta.Constants.*;
import static danta.aem.Constants.CLIENT_STATISTICS_CONTENT_MODEL_SELECTORS;
import static danta.aem.Constants.SLING_HTTP_REQUEST;

/**
 * This servlet returns the executing CPs for a given resource
 *
 * @author      jarriola
 * @version     1.0.0
 * @since       2018-01-18
 */
@Component
@Service
@Properties({
        @Property(name = "service.description", value = "Component Statistics Content Model"),
        @Property(name = "sling.servlet.selectors", value = CLIENT_STATISTICS_CONTENT_MODEL_SELECTORS),
        @Property(name = "sling.servlet.extensions", value = JSON),
        @Property(name = "sling.servlet.resourceTypes", value = "sling/servlet/default")
})
public class StatisticsServlet
        extends SlingSafeMethodsServlet {

    @Reference
    private ContentModelFactoryService contentModelFactoryService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY, policy = ReferencePolicy.STATIC)
    private ConfigurationProvider configurationProvider;

    @Reference
    private ContextProcessorEngine contextProcessorEngine;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        try {
            Resource resource = request.getResource();
            if (resource != null && configurationProvider.hasConfig(resource.getResourceType())) {

                JSONArray CPsList = new JSONArray();
                TemplateContentModel contentModel = (TemplateContentModel) contentModelFactoryService.getContentModel(request, response);

                ExecutionContextImpl executionContext = new ExecutionContextImpl();
                executionContext.put(SLING_HTTP_REQUEST, request);
                executionContext.put(ENGINE_RESOURCE, resource.getResourceType());

                List<String> currentProcessorChain = contextProcessorEngine.execute(executionContext, contentModel);

                for (String CP : currentProcessorChain) {
                    CPsList.add(CP);
                }

                out.write(CPsList.toString());
            } else {
                out.write(new JSONObject().toJSONString());
            }
        } catch (Exception ew) {
            throw new ServletException(ew);
        }
    }
}

