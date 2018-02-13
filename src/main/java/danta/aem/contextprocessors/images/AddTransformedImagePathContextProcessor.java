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

package danta.aem.contextprocessors.images;

import com.google.common.collect.Sets;
import danta.aem.assets.AssetPathService;
import danta.api.ContextProcessor;
import danta.api.ExecutionContext;
import danta.api.TemplateContentModel;
import danta.api.exceptions.ProcessException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.apache.sling.api.SlingHttpServletRequest;

import java.util.Collections;
import java.util.Set;

import static danta.Constants.*;
import static danta.aem.Constants.SLING_HTTP_REQUEST;

/**
 * The context processor for adding a transformed image path to the content model
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2016-03-06
 */
@Component(service = ContextProcessor.class)
public class AddTransformedImagePathContextProcessor
        extends AbstractImageContextProcessor<TemplateContentModel> {

    private static final Set<String> ANY_OF = Collections.unmodifiableSet(Sets.newHashSet(CONTENT_IMAGE_CATEGORY));

    @Reference
    AssetPathService assetPathService;

    @Override
    public Set<String> anyOf() {
        return ANY_OF;
    }

    @Override
    public void process(final ExecutionContext executionContext, TemplateContentModel contentModel)
            throws ProcessException {
        try {
            SlingHttpServletRequest request = (SlingHttpServletRequest) executionContext.get(SLING_HTTP_REQUEST);
            String imagePath = assetPathService.getComponentImagePath(request.getResource());
            contentModel.set(CONTENT + DOT + IMAGE_PATH, imagePath);
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }
}
