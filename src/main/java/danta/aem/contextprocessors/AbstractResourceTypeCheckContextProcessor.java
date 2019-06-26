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

import com.google.common.collect.Sets;
import danta.api.ContentModel;
import danta.api.ExecutionContext;
import danta.api.exceptions.AcceptsException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.Collections;
import java.util.Set;

import static danta.Constants.BLANK;
import static danta.aem.Constants.SLING_HTTP_REQUEST;

/**
 * The abstraction for Resource Type Check Context Processor
 *
 * @author antonio
 * @version 1.0.0
 * @since 2013-11-08
 */
public abstract class AbstractResourceTypeCheckContextProcessor<C extends ContentModel>
        extends AbstractCheckResourceExistenceContextProcessor<C> {

    @Override
    public boolean accepts(final ExecutionContext executionContext)
            throws AcceptsException {
        SlingHttpServletRequest request = (SlingHttpServletRequest) executionContext.get(SLING_HTTP_REQUEST);
        boolean accepts = false;
        Resource resource = request.getResource();
        if (super.accepts(executionContext)) {
            Set<String> resourceTypes = requiredResourceTypes();
            if (resourceTypes == null || resourceTypes.isEmpty()) {
                resourceTypes = Sets.newHashSet(requiredResourceType());
            }
            ResourceResolver resolver = null;
            try {
                resolver = request.getResourceResolver();
                for (String resourceType : resourceTypes) {
                    if (resolver.isResourceType(resource, resourceType)) {
                        accepts = true;
                        break;
                    }
                }
            } catch (Exception ew) {
                throw new AcceptsException(ew);
            } finally {
                if (resolver != null)
                    resolver.close();
            }
        }
        return accepts;
    }

    /**
     * This method must be used to set the ResourceType to be compared on the accepts() method.
     *
     * @return the ResourceType String.
     */
    public String requiredResourceType() {
        return BLANK;
    }

    /**
     * @return resourceTypes The set of resource types
     */
    public Set<String> requiredResourceTypes() {
        return Collections.emptySet();
    }
}
