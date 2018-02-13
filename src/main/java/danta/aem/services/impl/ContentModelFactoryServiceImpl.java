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

package danta.aem.services.impl;

import danta.aem.services.ContentModelFactoryService;
import danta.aem.templating.TemplateContentModelImpl;
import danta.api.ContentModel;
import danta.api.ContextProcessorEngine;
import danta.api.configuration.ConfigurationProvider;
import danta.core.execution.ExecutionContextImpl;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static danta.Constants.CONFIGURATION_PROVIDER;
import static danta.Constants.ENGINE_RESOURCE;
import static danta.aem.Constants.SLING_HTTP_REQUEST;

/**
 * The Content Model Factory Service implementer
 *
 * @author      Danta Team
 * @version     1.0.0
 * @since       2014-03-12
 */
@Component(service = ContentModelFactoryService.class)
public class ContentModelFactoryServiceImpl
        implements ContentModelFactoryService {

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
    protected ContextProcessorEngine contextProcessorEngine;

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
    private ConfigurationProvider configurationProvider;

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

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
                executionContext.put(CONFIGURATION_PROVIDER, configurationProvider);
                contextProcessorEngine.execute(executionContext, contentModel);
            }

        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return contentModel;
    }

}
