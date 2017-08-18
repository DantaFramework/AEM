/**
 * LayerX AEM Bundle
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

package layerx.aem.contextprocessors;

import com.day.cq.wcm.api.AuthoringUIMode;
import com.day.cq.wcm.api.components.ComponentManager;
import com.google.common.collect.Sets;
import layerx.aem.templating.TemplateContentModelImpl;
import layerx.aem.util.PropertyUtils;
import layerx.aem.util.ResourceUtils;
import layerx.api.ExecutionContext;
import layerx.api.configuration.Configuration;
import layerx.api.exceptions.ProcessException;
import layerx.core.contextprocessors.AbstractCheckComponentCategoryContextProcessor;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import javax.jcr.Node;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static layerx.Constants.*;
import static layerx.aem.Constants.*;
import static layerx.core.util.ObjectUtils.wrap;

/**
 * The context processor for adding component properties to content model
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2013-11-04
 */
@Component
@Service
public class AddComponentPropertiesContextProcessor
        extends AbstractCheckComponentCategoryContextProcessor<TemplateContentModelImpl> {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY, policy = ReferencePolicy.STATIC)
    private ResourceResolverFactory resourceResolverFactory;

    private static final String CONFIG_SERVICE = "config-service";

    @Override
    public Set<String> anyOf() {
        return Sets.newHashSet(COMPONENT_CATEGORY);
    }

    @Override
    public int priority() {
        // This processor must be one of the first processors executed.
        return HIGHEST_PRIORITY;
    }

    @Override
    public void process(final ExecutionContext executionContext, TemplateContentModelImpl contentModel)
            throws ProcessException {

        try {
            SlingHttpServletRequest request = (SlingHttpServletRequest) executionContext.get(SLING_HTTP_REQUEST);
            Resource resource = request.getResource();
            if (resource != null) {

                Map<String, Object> authenticationInfo = new HashMap<String, Object>();
                authenticationInfo.put(ResourceResolverFactory.SUBSERVICE, CONFIG_SERVICE);
                ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(authenticationInfo);

                ComponentManager componentManager = resourceResolver.adaptTo(ComponentManager.class);
                com.day.cq.wcm.api.components.Component component = componentManager.getComponentOfResource(resource);
                if (component != null) {
                    Configuration configuration = configurationProvider.getFor(resource.getResourceType());
                    contentModel.setAsIsolated(CONFIG_PROPERTIES_KEY, wrap(configuration.toMap()));
                    Resource componentResource = component.adaptTo(Resource.class);
                    Node componentNode = componentResource.adaptTo(Node.class);
                    Map<String, Object> componentProps = PropertyUtils.propsToMap(componentNode.getProperties());
                    componentProps.put(PATH, componentResource.getPath());
                    componentProps.put(APP_NAME_PROPERTY_KEY, ResourceUtils.getAppName(resource));
                    String globalDialogPath = getGlobalDialogPath(resource, resourceResolver, request);
                    if (!BLANK.equals(globalDialogPath)) {
                        componentProps.put(GLOBAL_DIALOG_PATH_PROPERTY_KEY, globalDialogPath);
                    }
                    String globalPropertiesPath = ResourceUtils.getGlobalPropertiesPath(resource, resourceResolver);
                    if (!BLANK.equals(globalPropertiesPath)) {
                        componentProps.put(GLOBAL_PATH_PROPERTY_KEY, globalPropertiesPath);
                    }
                    contentModel.setAsIsolated(COMPONENT_PROPERTIES_KEY, componentProps);
                }
                resourceResolver.close();

            }
        } catch (Exception e) {
            throw new ProcessException(e);
        }

    }

    private String getGlobalDialogPath(Resource resource, ResourceResolver resourceResolver, SlingHttpServletRequest request) {
        String currentGlobalDialogPath = GLOBAL_DIALOG_PATH;
        if (AuthoringUIMode.fromRequest(request) == AuthoringUIMode.TOUCH) {
            currentGlobalDialogPath = GLOBAL_DIALOG_PATH_TOUCH;
        }
        String globalDialogPath = APPS_ROOT + "/" + ResourceUtils.getAppName(resource) + currentGlobalDialogPath;
        if (null == resourceResolver.getResource(globalDialogPath)) {
            globalDialogPath = BLANK;
        }
        return globalDialogPath;
    }
}
