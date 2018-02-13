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

package danta.aem.contextprocessors.lists;

import danta.api.ContextProcessor;
import danta.api.ExecutionContext;
import danta.api.TemplateContentModel;
import danta.api.configuration.Configuration;
import danta.api.configuration.ConfigurationProvider;
import danta.api.configuration.Mode;
import danta.api.exceptions.ProcessException;
import org.osgi.service.component.annotations.Component;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.Collection;

import static danta.Constants.*;
import static danta.aem.Constants.SLING_HTTP_REQUEST;

/**
 * The context processor for adding item list to the content model
 *
 * @author      Danta Team
 * @version     1.0.0
 * @since       2014-08-16
 */
@Component(service = ContextProcessor.class)
public class AddItemListConfigsContextProcessor
        extends AbstractItemListContextProcessor<TemplateContentModel> {

    protected static final int PRIORITY = AddItemListContextProcessor.PRIORITY - 10;

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
    private ConfigurationProvider configurationProvider;

    @Override
    public int priority() {
        return PRIORITY;
    }

    @Override
    public void process(final ExecutionContext executionContext, TemplateContentModel contentModel)
            throws ProcessException {
        try {
            SlingHttpServletRequest request = (SlingHttpServletRequest) executionContext.get(SLING_HTTP_REQUEST);
            Resource resource = request.getResource();
            Configuration configuration = configurationProvider.getFor(resource.getResourceType());
            Collection<String> listClasses = configuration.asStrings(LIST_CLASSES_CONFIG_PROP, Mode.MERGE);
            contentModel.set(LIST_PROPERTIES_KEY + "." + LIST_CLASSES_PROP, listClasses);
            Collection<String> itemClasses = configuration.asStrings(LIST_ITEM_CLASSES_CONFIG_PROP, Mode.MERGE);
            contentModel.set(LIST_PROPERTIES_KEY + "." + LIST_ITEM_CLASSES_PROP, itemClasses);
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }
}
