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

package layerx.aem.contextprocessors.images;

import com.google.common.collect.Sets;
import layerx.aem.assets.AssetPathService;
import layerx.api.ExecutionContext;
import layerx.api.TemplateContentModel;
import layerx.api.exceptions.ProcessException;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;

import java.util.Set;

import static layerx.Constants.*;
import static layerx.aem.Constants.SLING_HTTP_REQUEST;

/**
 * The context processor for adding a transformed image path to the content model
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2016-03-06
 */
@Component
@Service
public class AddTransformedImagePathContextProcessor
        extends AbstractImageContextProcessor<TemplateContentModel> {

    @Reference
    AssetPathService assetPathService;

    @Override
    public Set<String> anyOf() {
        return Sets.newHashSet(CONTENT_IMAGE_CATEGORY);
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
