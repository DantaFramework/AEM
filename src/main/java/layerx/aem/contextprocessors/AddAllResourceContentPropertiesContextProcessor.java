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

import com.google.common.collect.Sets;
import layerx.aem.templating.TemplateContentModelImpl;
import layerx.api.ExecutionContext;
import layerx.api.exceptions.ProcessException;
import layerx.core.contextprocessors.AbstractCheckComponentCategoryContextProcessor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

import javax.jcr.Node;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static layerx.Constants.*;
import static layerx.aem.Constants.SLING_HTTP_REQUEST;
import static layerx.aem.util.PropertyUtils.propsToMap;
import static layerx.core.Constants.XK_CONTENT_ID_CP;

/**
 * The context processor for adding all resource content properties to content model
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2013-11-04
 */
@Component
@Service
public class AddAllResourceContentPropertiesContextProcessor
        extends AbstractCheckComponentCategoryContextProcessor<TemplateContentModelImpl> {

    @Override
    public Set<String> anyOf() {
        return Sets.newHashSet(CONTENT_CATEGORY);
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
            Map<String, Object> content = new HashMap<>();
            if (resource != null) {
                Node node = resource.adaptTo(Node.class);
                if (node != null) {
                    String componentContentId = DigestUtils.md5Hex(resource.getPath());
                    content.put(XK_CONTENT_ID_CP, componentContentId);
                    Map<String, Object> propsMap = propsToMap(node.getProperties());
                    for (String propertyName : propsMap.keySet()) {
                        if (!StringUtils.startsWithAny(propertyName, RESERVED_SYSTEM_NAME_PREFIXES)) {
                            content.put(propertyName, propsMap.get(propertyName));
                        }
                    }
                    content.put(ID, DigestUtils.md5Hex(resource.getPath()));
                } else {
                    // the resource doesn't exist so we clear the content
                    content.clear();
                }
            } else {
                content.put(XK_CONTENT_ID_CP, "_NONE");
            }
            content.put(PATH, resource.getPath());
            content.put(NAME, resource.getName());
            contentModel.set(RESOURCE_CONTENT_KEY, content);
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }
}
