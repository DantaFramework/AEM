/**
 * Danta AEM Bundle
 * <p>
 * Copyright (C) 2017 Tikal Technologies, Inc. All rights reserved.
 * <p>
 * Licensed under GNU Affero General Public License, Version v3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.gnu.org/licenses/agpl-3.0.txt
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied;
 * without even the implied warranty of MERCHANTABILITY.
 * See the License for more details.
 */

package danta.aem.contextprocessors;

import com.day.cq.wcm.api.AuthoringUIMode;
import com.day.cq.wcm.api.components.ComponentManager;
import com.google.common.collect.Sets;
import danta.aem.templating.TemplateContentModelImpl;
import danta.aem.util.PropertyUtils;
import danta.aem.util.ResourceUtils;
import danta.api.ExecutionContext;
import danta.api.configuration.Configuration;
import danta.api.exceptions.ProcessException;
import danta.core.contextprocessors.AbstractCheckComponentCategoryContextProcessor;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import javax.jcr.Node;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static danta.Constants.*;
import static danta.aem.Constants.*;
import static danta.core.util.ObjectUtils.wrap;

/**
 * The context processor for adding component properties to content model
 *
 * @author joshuaoransky
 * @version 1.0.0
 * @since 2013-11-04
 */
@Component
@Service
public class AddComponentPropertiesContextProcessor
        extends AbstractCheckComponentCategoryContextProcessor<TemplateContentModelImpl> {

    private static final Set<String> ANY_OF = Collections.unmodifiableSet(Sets.newHashSet(COMPONENT_CATEGORY));

    private static final String CONFIG_SERVICE = "config-service";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY, policy = ReferencePolicy.STATIC)
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public Set<String> anyOf() {
        return ANY_OF;
    }

    @Override
    public int priority() {
        // This processor must be one of the first processors executed.
        return HIGHEST_PRIORITY;
    }

    @Override
    public void process(final ExecutionContext executionContext, TemplateContentModelImpl contentModel)
            throws ProcessException {
        ResourceResolver resolver = null;
        try {
            SlingHttpServletRequest request = (SlingHttpServletRequest) executionContext.get(SLING_HTTP_REQUEST);
            Resource resource = request.getResource();
            if (resource != null) {

                Map<String, Object> authenticationInfo = new HashMap<String, Object>();
                authenticationInfo.put(ResourceResolverFactory.SUBSERVICE, CONFIG_SERVICE);


                resolver = resourceResolverFactory.getServiceResourceResolver(authenticationInfo);


                ComponentManager componentManager = resolver.adaptTo(ComponentManager.class);
                com.day.cq.wcm.api.components.Component component = componentManager.getComponentOfResource(resource);
                if (component != null) {
                    Configuration configuration = configurationProvider.getFor(resource.getResourceType());
                    contentModel.setAsIsolated(CONFIG_PROPERTIES_KEY, wrap(configuration.toMap()));
                    Resource componentResource = component.adaptTo(Resource.class);
                    Node componentNode = componentResource.adaptTo(Node.class);
                    Map<String, Object> componentProps = PropertyUtils.propsToMap(componentNode.getProperties());
                    componentProps.put(PATH, componentResource.getPath());
                    componentProps.put(APP_NAME_PROPERTY_KEY, ResourceUtils.getAppName(resource));
                    String globalDialogPath = getGlobalDialogPath(resource, resolver, request);
                    if (!BLANK.equals(globalDialogPath)) {
                        componentProps.put(GLOBAL_DIALOG_PATH_PROPERTY_KEY, globalDialogPath);
                    }
                    String globalPropertiesPath = ResourceUtils.getGlobalPropertiesPath(resource, resolver);
                    if (!BLANK.equals(globalPropertiesPath)) {
                        componentProps.put(GLOBAL_PATH_PROPERTY_KEY, globalPropertiesPath);
                    }
                    contentModel.setAsIsolated(COMPONENT_PROPERTIES_KEY, componentProps);
                }
                resolver.close();

            }
        } catch (Exception e) {
            throw new ProcessException(e);
        } finally {
            if (resolver != null)
                resolver.close();
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
