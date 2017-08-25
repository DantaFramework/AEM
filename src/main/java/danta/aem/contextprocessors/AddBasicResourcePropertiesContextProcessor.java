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
import danta.api.ExecutionContext;
import danta.api.exceptions.ProcessException;
import danta.core.contextprocessors.AbstractCheckComponentCategoryContextProcessor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

import javax.jcr.Node;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static danta.Constants.*;
import static danta.aem.Constants.SLING_HTTP_REQUEST;
import static danta.core.Constants.XK_CONTENT_ID_CP;

/**
 * The context processor for adding basic resource properties to content model
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2013-11-04
 */
@Component
@Service
public class AddBasicResourcePropertiesContextProcessor
        extends AbstractCheckComponentCategoryContextProcessor<TemplateContentModelImpl> {

    @Override
    public Set<String> anyOf() {
        return Sets.newHashSet(CONTENT_CATEGORY);
    }

    @Override
    public int priority() {
        return HIGHEST_PRIORITY - 10;
    }

    @Override
    public void process(final ExecutionContext executionContext, final TemplateContentModelImpl contentModel)
            throws ProcessException {
        SlingHttpServletRequest request = (SlingHttpServletRequest) executionContext.get(SLING_HTTP_REQUEST);
        Resource resource = request.getResource();
        Map<String, Object> content = (contentModel.has(RESOURCE_CONTENT_KEY)) ? ((Map<String, Object>) contentModel.get(RESOURCE_CONTENT_KEY)) : new HashMap<String, Object>();

        if (resource != null) {
            Node node = resource.adaptTo(Node.class);
            if (node != null) {
                String componentContentId = DigestUtils.md5Hex(resource.getPath());
                content.put(XK_CONTENT_ID_CP, componentContentId);
            } else {
                content.put(XK_CONTENT_ID_CP, "_NONE");
            }
            content.put(PATH, resource.getPath());
            content.put(NAME, resource.getName());
        }
    }
}
