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

import com.day.cq.wcm.api.TemplateManager;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.commons.WCMUtils;
import com.day.cq.wcm.foundation.TemplatedContainer;
import com.google.common.collect.Sets;
import danta.aem.templating.TemplateContentModelImpl;
import danta.api.ExecutionContext;
import danta.api.exceptions.ProcessException;
import danta.core.contextprocessors.AbstractCheckComponentCategoryContextProcessor;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

import java.util.*;

import static danta.Constants.*;
import static danta.aem.Constants.SLING_HTTP_REQUEST;
import static danta.aem.Constants.STRUCTURE_RESOURCES_CATEGORY;

/**
 * This Context Processor allows to add data of the structure resources to the content model.
 * This data can be used to include that structures resources in the content of dynamic pages.
 *
 * @author      jbarrera
 * @version     1.0.0
 * @since       2017-11-22
 */
@Component
@Service
public class AddStructureResourcesContextProcessor
        extends AbstractCheckComponentCategoryContextProcessor<TemplateContentModelImpl> {

    private static final Set<String> ANY_OF = Collections.unmodifiableSet(Sets.newHashSet(STRUCTURE_RESOURCES_CATEGORY));
    private static final String STRUCTURE_RESOURCES = "structureResources";

    @Override
    public Set<String> anyOf() {
        return ANY_OF;
    }

    @Override
    public int priority() {
        return HIGHEST_PRIORITY;
    }

    @Override
    public void process(final ExecutionContext executionContext, final TemplateContentModelImpl contentModel)
            throws ProcessException {
        try {
            SlingHttpServletRequest request = (SlingHttpServletRequest) executionContext.get(SLING_HTTP_REQUEST);

            ComponentContext componentContext = WCMUtils.getComponentContext(request);
            TemplateManager templateManager = request.getResourceResolver().adaptTo(TemplateManager.class);

            if (templateManager != null) {
                TemplatedContainer templatedContainer = new TemplatedContainer(templateManager, componentContext);

                if (templatedContainer.hasStructureSupport()) {
                    List<Resource> structureResources = templatedContainer.getStructureResources();
                    Map<String, Object> structureResourcesMap = new HashMap<>();

                    for (Resource resource: structureResources) {
                        Map<String, String> resourceProperties = new HashMap<>();
                        resourceProperties.put("type", resource.getResourceType());
                        resourceProperties.put("path", resource.getPath());

                        structureResourcesMap.put(resource.getName(), resourceProperties);
                    }
                    contentModel.set(CONTENT + DOT + STRUCTURE_RESOURCES, structureResourcesMap);
                }
            }
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }
}

