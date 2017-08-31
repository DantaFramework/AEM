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

package danta.aem.contextprocessors;

import com.google.common.collect.Sets;
import danta.aem.templating.TemplateContentModelImpl;
import danta.aem.util.PropertyUtils;
import danta.aem.util.ResourceUtils;
import danta.api.ExecutionContext;
import danta.api.exceptions.ProcessException;
import danta.core.contextprocessors.AbstractCheckComponentCategoryContextProcessor;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static danta.Constants.*;
import static danta.aem.Constants.SLING_HTTP_REQUEST;

/**
 * The context processor for adding global properties to content model
 *
 * @author      palecio
 * @version     1.0.0
 * @since       2014-09-04
 */
@Component
@Service
public class AddGlobalPropertiesContextProcessor
        extends AbstractCheckComponentCategoryContextProcessor<TemplateContentModelImpl> {

    private static final Set<String> ANY_OF = Collections.unmodifiableSet(Sets.newHashSet(GLOBAL_CATEGORY));

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
    public void process(final ExecutionContext executionContext, final TemplateContentModelImpl contentModel)
            throws ProcessException {
        try {
            SlingHttpServletRequest request = (SlingHttpServletRequest) executionContext.get(SLING_HTTP_REQUEST);
            Resource resource = request.getResource();
            log.debug("for {}", resource.getPath());
            if (resource != null) {
                ResourceResolver resourceResolver = request.getResourceResolver();
                String globalPropertiesPath = ResourceUtils.getGlobalPropertiesPath(resource, resourceResolver);

                if (globalPropertiesPath != null) {
                    Resource globalPropertiesResource = resourceResolver.getResource(globalPropertiesPath);
                    Map<String, Object> globalProperties = (globalPropertiesResource != null) ? PropertyUtils.propsToMap(globalPropertiesResource) : new HashMap<String, Object>();
                    contentModel.set(GLOBAL_PROPERTIES_KEY, globalProperties);
                }
            }
        } catch (Exception e) {
            throw new ProcessException(e);
        }

    }
}
