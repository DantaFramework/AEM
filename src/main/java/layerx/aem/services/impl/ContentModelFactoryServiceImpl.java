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

package layerx.aem.services.impl;

import layerx.aem.services.ContentModelFactoryService;
import layerx.aem.templating.TemplateContentModelImpl;
import layerx.api.ContentModel;
import layerx.api.ContextProcessorEngine;
import layerx.core.execution.ExecutionContextImpl;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static layerx.Constants.ENGINE_RESOURCE;
import static layerx.aem.Constants.SLING_HTTP_REQUEST;

/**
 * The Content Model Factory Service implementer
 *
 * @author      LayerX Team
 * @version     1.0.0
 * @since       2014-03-12
 */
@Component
@Service(ContentModelFactoryService.class)
public class ContentModelFactoryServiceImpl
        implements ContentModelFactoryService {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY, policy = ReferencePolicy.STATIC)
    protected ContextProcessorEngine contextProcessorEngine;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * This method gets the Content Model of a resource.
     *
     * @param request This is a SlingHttpServletRequest
     * @param response This is a SlingHttpServletResponse
     * @return contentModel The Content Model of a resource
     */
    @Override
    public ContentModel getContentModel (final SlingHttpServletRequest request, final SlingHttpServletResponse response) {
        final ContentModel contentModel = new TemplateContentModelImpl(request, response);
        try {
            Resource resource = request.getResource();
            if (resource != null) {
                ExecutionContextImpl executionContext = new ExecutionContextImpl();
                executionContext.put(SLING_HTTP_REQUEST, request);
                executionContext.put(ENGINE_RESOURCE, resource.getResourceType());
                contextProcessorEngine.execute(executionContext, contentModel);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return contentModel;
    }

}
