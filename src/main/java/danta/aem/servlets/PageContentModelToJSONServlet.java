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

import com.cognifide.sling.query.api.Predicate;
import com.cognifide.sling.query.api.SearchStrategy;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import static com.cognifide.sling.query.api.SlingQuery.$;
import static danta.Constants.*;
import static danta.core.Constants.*;
import static danta.aem.Constants.CLIENT_PAGE_CONTENT_MODEL_SELECTORS;
import static org.apache.jackrabbit.JcrConstants.NT_UNSTRUCTURED;

/**
 * This is a Page Content Model to JSON Servlet
 *
 * @author      jbarrera
 * @version     1.0.0
 * @since       2014-04-17
 */
@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.extensions=" + JSON,
                "sling.servlet.selectors=" + CLIENT_PAGE_CONTENT_MODEL_SELECTORS,
                "sling.servlet.resourceTypes=sling/servlet/default",
        }
)
public class PageContentModelToJSONServlet
        extends SlingSafeMethodsServlet {

    @Reference
    private ContentModelFactoryService contentModelFactoryService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
    private ConfigurationProvider configurationProvider;

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

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

                // add component list with clientaccessible as true on the resource page
                filteredContentMap.put(PAGE_COMPONENT_RESOURCE_LIST_AN, getComponentList(resource));

                out.write(JSONObject.toJSONString(filteredContentMap));
            } else {
                out.write(new JSONObject().toJSONString());
            }
        } catch (Exception ew) {
            throw new ServletException(ew);
        }
    }


    /**
     * This method gets the component list with the clientaccessible property as true on the current page resource
     *
     * @param resource
     * @return a jsonobject with the component list
     */
    private JSONObject getComponentList(Resource resource) {
        JSONObject componentContentModels = new JSONObject();
        int prefixLength = resource.getPath().length();
        List<Resource> pageComponentResources = $(resource).searchStrategy(SearchStrategy.QUERY).find(NT_UNSTRUCTURED).filter(SECTION_BASE_PREDICATE).asList();
        if (pageComponentResources != null && pageComponentResources.size() > 0) {
            for (Resource componentResource : pageComponentResources) {
                componentContentModels.put(DigestUtils.md5Hex(componentResource.getPath()), componentResource.getPath().substring(prefixLength));
            }
        }
        return componentContentModels;
    }

    private final Predicate<Resource> SECTION_BASE_PREDICATE = new Predicate<Resource>() {
        @Override
        public boolean accepts(Resource resource) {
            boolean accepts = false;
            try {
                if (resource != null) {
                    Configuration configuration = configurationProvider.getFor(resource.getResourceType());
                    String propClientAccessible = configuration.asString(XK_CLIENT_ACCESSIBLE_CP);
                    Boolean clientAccessible = Boolean.valueOf(propClientAccessible);
                    if (clientAccessible) {
                        accepts = true;
                    }
                }
            } catch (Exception e) {
                LOG.debug(e.toString());
            }
            return accepts;
        }
    };

}

