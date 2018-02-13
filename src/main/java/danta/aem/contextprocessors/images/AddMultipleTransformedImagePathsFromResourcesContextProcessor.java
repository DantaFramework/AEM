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

import com.cognifide.sling.query.api.Predicate;
import com.cognifide.sling.query.api.SearchStrategy;
import com.google.common.collect.Sets;
import danta.aem.assets.AssetPathService;
import danta.api.ContextProcessor;
import danta.api.ExecutionContext;
import danta.api.TemplateContentModel;
import danta.api.exceptions.ProcessException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.cognifide.sling.query.api.SlingQuery.$;
import static danta.Constants.*;
import static danta.aem.Constants.FOUNDATION_IMAGE_COMPONENT_RESOURCE_TYPE;
import static danta.aem.Constants.SLING_HTTP_REQUEST;
import static org.apache.jackrabbit.JcrConstants.NT_UNSTRUCTURED;

/**
 * The context processor for adding multiple transformed image paths, from resources, to the content model
 *
 * @author      jbarrera
 * @version     1.0.0
 * @since       2016-07-11
 */
@Component(service = ContextProcessor.class)
public class AddMultipleTransformedImagePathsFromResourcesContextProcessor
        extends AbstractImageContextProcessor<TemplateContentModel> {

    private static final Set<String> ANY_OF = Collections.unmodifiableSet(Sets.newHashSet(MULTIPLE_IMAGE_RESOURCES_CATEGORY));

    @Reference
    AssetPathService assetPathService;

    @Override
    public Set<String> anyOf() {
        return ANY_OF;
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
            Collection<String> imagePathList = new ArrayList<>();
            for (Resource imageResource : imageResources) {
                imagePathList.add(assetPathService.getComponentImagePath(imageResource));
            }
            contentModel.set(CONTENT + DOT + IMAGE_PATHS_FROM_RESOURCES, imagePathList);
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }
}
