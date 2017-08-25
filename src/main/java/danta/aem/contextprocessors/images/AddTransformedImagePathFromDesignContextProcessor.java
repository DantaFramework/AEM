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

import com.day.cq.wcm.api.designer.Style;
import com.google.common.collect.Sets;
import danta.aem.assets.AssetPathService;
import danta.aem.util.GeneralRequestObjects;
import danta.api.ExecutionContext;
import danta.api.TemplateContentModel;
import danta.api.exceptions.ProcessException;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.Set;

import static danta.Constants.*;
import static danta.aem.Constants.SLING_HTTP_REQUEST;

/**
 * The context processor for adding a transformed image path, form design, to the content model
 *
 * @author      palecio
 * @version     1.0.0
 * @since       2016-03-25
 */
@Component
@Service
public class AddTransformedImagePathFromDesignContextProcessor
        extends AbstractImageContextProcessor<TemplateContentModel> {

    @Reference
    private AssetPathService assetPathService;

    @Override
    public Set<String> anyOf() {
        return Sets.newHashSet(DESIGN_IMAGE_CATEGORY);
    }

    @Override
    public void process(final ExecutionContext executionContext, TemplateContentModel contentModel)
            throws ProcessException {
        try {
            SlingHttpServletRequest request = (SlingHttpServletRequest) executionContext.get(SLING_HTTP_REQUEST);
            ResourceResolver resourceResolver = request.getResourceResolver();
            Style style = GeneralRequestObjects.getCurrentStyle(request);
            if (style != null) {
                Resource designResource = resourceResolver.getResource(style.getPath()); //get design resource
                if (designResource != null) {
                    String imagePath = assetPathService.getComponentImagePath(designResource);
                    contentModel.set(DESIGN_PROPERTIES_KEY + DOT + IMAGE_PATH, imagePath);
                }
            }
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }

}
