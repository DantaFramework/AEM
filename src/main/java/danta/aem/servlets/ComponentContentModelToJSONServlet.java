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
import danta.api.TemplateContentModel;
import danta.api.configuration.Configuration;
import danta.api.configuration.ConfigurationProvider;
import danta.api.configuration.Mode;
import net.minidev.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import static danta.Constants.*;
import static danta.core.Constants.*;
import static danta.aem.Constants.CLIENT_COMPONENT_CONTENT_MODEL_SELECTORS;

/**
 * This is a servlet used to turns component content model to json servlet
 *
 * @author      jbarrera
 * @version     1.0.0
 * @since       2013-04-17
 */
@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.extensions=" + JSON,
                "sling.servlet.selectors=" + CLIENT_COMPONENT_CONTENT_MODEL_SELECTORS,
                "sling.servlet.resourceTypes=sling/servlet/default",
        }
)
public class ComponentContentModelToJSONServlet
        extends SlingSafeMethodsServlet {

    @Reference
    private ContentModelFactoryService contentModelFactoryService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
    private ConfigurationProvider configurationProvider;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        try {
            Resource resource = request.getResource();
            if (resource != null && configurationProvider.hasConfig(resource.getResourceType())) {
                JSONObject filteredContentMap = new JSONObject();
                TemplateContentModel templateContentModel = (TemplateContentModel) contentModelFactoryService.getContentModel(request, response);

                boolean clientAccessible =  (Boolean) templateContentModel.get(CONFIG_PROPERTIES_KEY + DOT + XK_CLIENT_ACCESSIBLE_CP);
                if (clientAccessible) {
                    // get list of contexts
                    Configuration configuration = configurationProvider.getFor(resource.getResourceType());
                    Collection<String> props = configuration.asStrings(XK_CLIENT_MODEL_PROPERTIES_CP, Mode.MERGE);
                    String[] contexts = props.toArray(new String[0]);

                    // get content model json with the XK_CLIENT_MODEL_PROPERTIES_CP contexts
                    filteredContentMap = templateContentModel.toJSONObject(contexts);

                    // add component id
                    String componentContentId = DigestUtils.md5Hex(resource.getPath());
                    filteredContentMap.put(XK_CONTENT_ID_CP, componentContentId);
                }
                out.write(JSONObject.toJSONString(filteredContentMap));
            } else {
                out.write(new JSONObject().toJSONString());
            }
        } catch (Exception ew) {
            throw new ServletException(ew);
        }
    }
}

