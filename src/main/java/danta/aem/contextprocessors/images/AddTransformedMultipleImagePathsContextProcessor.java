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
import danta.aem.util.ResourceUtils;
import danta.api.ExecutionContext;
import danta.api.TemplateContentModel;
import danta.api.exceptions.ProcessException;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static danta.Constants.*;
import static danta.aem.Constants.SLING_HTTP_REQUEST;

/**
 * The context processor for adding a transformed multiple image paths to the content model
 *
 * @author      palecio
 * @version     1.0.0
 * @since       2014-10-10
 */
@Component
@Service
public class AddTransformedMultipleImagePathsContextProcessor
        extends AbstractImageContextProcessor<TemplateContentModel> {

    @Reference
    AssetPathService assetPathService;

    @Override
    public Set<String> anyOf() {
        return Sets.newHashSet(MULTIPLE_IMAGE_CATEGORY);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(final ExecutionContext executionContext, TemplateContentModel contentModel)
            throws ProcessException {
        try {
            SlingHttpServletRequest request = (SlingHttpServletRequest) executionContext.get(SLING_HTTP_REQUEST);
            Resource resource = request.getResource();
            Collection<String> fileReferences = ResourceUtils.getPropertyAsStrings(resource, FILE_REFERENCES);
            Collection<String> imagePathList = new ArrayList<>();
            for(int i = 0; i < fileReferences.size(); i++) {
                imagePathList.add(assetPathService.getComponentAssetPath(resource, i));
            }
            contentModel.set(CONTENT + DOT + IMAGE_PATHS, imagePathList);

        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }
}
