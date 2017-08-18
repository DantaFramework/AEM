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

import com.cognifide.sling.query.api.Predicate;
import com.cognifide.sling.query.api.SearchStrategy;
import com.google.common.collect.Sets;
import layerx.aem.assets.AssetPathService;
import layerx.api.ExecutionContext;
import layerx.api.TemplateContentModel;
import layerx.api.exceptions.ProcessException;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.cognifide.sling.query.api.SlingQuery.$;
import static layerx.Constants.*;
import static layerx.aem.Constants.FOUNDATION_IMAGE_COMPONENT_RESOURCE_TYPE;
import static layerx.aem.Constants.SLING_HTTP_REQUEST;
import static org.apache.jackrabbit.JcrConstants.NT_UNSTRUCTURED;

/**
 * The context processor for adding a transformed image path to the content model
 *
 * @author      jbarrera
 * @version     1.0.0
 * @since       2016-03-06
 */
@Component
@Service
public class AddTransformedImagePathsContextProcessor
        extends AbstractImageContextProcessor<TemplateContentModel> {

    @Reference
    AssetPathService assetPathService;

    @Override
    public Set<String> anyOf() {
        return Sets.newHashSet(IMAGES_BY_KEY_CATEGORY);
    }

    private final Predicate<Resource> IMAGE_RESOURCES_PREDICATE = new Predicate<Resource>() {
        @Override
        public boolean accepts(Resource resource) {
            return resource.isResourceType(FOUNDATION_IMAGE_COMPONENT_RESOURCE_TYPE);
        }
    };

    @Override
    public void process(final ExecutionContext executionContext, TemplateContentModel contentModel)
            throws ProcessException {
        try {
            SlingHttpServletRequest request = (SlingHttpServletRequest) executionContext.get(SLING_HTTP_REQUEST);
            Resource resource = request.getResource();
            List<Resource> imageResources = $(resource).searchStrategy(SearchStrategy.QUERY).find(NT_UNSTRUCTURED).filter(IMAGE_RESOURCES_PREDICATE).asList();
            Map<String, String> imagePathList = new HashMap<>();
            for (Resource imageResource : imageResources) {
                imagePathList.put(imageResource.getName(), assetPathService.getComponentImagePath(imageResource));
            }
            contentModel.set(CONTENT + DOT + IMAGE_PATHS_FROM_RESOURCES, imagePathList);
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }

}
