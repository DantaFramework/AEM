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

package danta.aem.assets.impl;

import com.day.cq.wcm.api.Page;
import com.day.text.Text;
import danta.aem.assets.AssetPathService;
import danta.aem.util.ImageUtils;
import danta.aem.util.ResourceUtils;
import danta.api.configuration.Configuration;
import danta.api.configuration.ConfigurationProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

import static danta.Constants.*;
import static danta.aem.Constants.*;

/**
 * Asset path service implementer
 *
 * @author      palecio
 * @version     1.0.0
 * @since       2014-09-22
 */
@Component
@Service
public class AssetPathServiceImpl implements AssetPathService {

    protected final Logger LOGGER = LoggerFactory.getLogger(AssetPathServiceImpl.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY, policy = ReferencePolicy.STATIC)
    protected ConfigurationProvider configurationProvider;

    /**
     * Get image path via a page and component's resource
     *
     * @param page The page to retrieve the image path from
     * @param componentResource The component resource that contains the image path
     * @return pageImagePath
     * @throws Exception
     */
    public String getPageImagePath(Page page, Resource componentResource) throws Exception {
        String pageImagePath = BLANK;
        Resource imageResource = page.getContentResource(IMAGE);
        if (null != imageResource) {
            String renditionName = getRenditionName(componentResource);
            pageImagePath = getComponentAssetPath(imageResource, renditionName);
        }
        return pageImagePath;
    }

    /**
     * For backward compatibility purposes
     *
     * @param componentResource A content resource containing a fileReference property.
     * @return componentImagePath The path to the image
     */
    public String getComponentImagePath(Resource componentResource) throws Exception {
        return getComponentAssetPath(componentResource);
    }

    /**
     * @param componentResource A content resource containing a fileReference property.
     * @return componentAssetPath The path to the image
     */
    public String getComponentAssetPath(Resource componentResource) throws Exception {
        return getComponentAssetPath(componentResource, BLANK, -1);
    }

    /**
     * @param componentResource The component's resource to fetch the componentAssetPath from
     * @return componentAssetPath The path to the image
     */
    public String getComponentAssetPath(Resource componentResource, String renditionName) throws Exception {
        return getComponentAssetPath(componentResource, renditionName, -1);
    }

    /**
     * @param componentResource The component's resource to fetch the componentAssetPath from
     * @param assetIndex The position of the asset path under the resource
     * @return componentAssetPath The path to the image
     */
    public String getComponentAssetPath(Resource componentResource, int assetIndex) throws Exception {
        return getComponentAssetPath(componentResource, BLANK, assetIndex);
    }

    /**
     * @param componentResource A content resource containing a fileReference property.
     * @param renditionName The name of the rendition node
     * @param assetIndex The position of the asset path under the resource
     * @return componentAssetPath The path to the image
     */
    public String getComponentAssetPath(Resource componentResource, String renditionName, int assetIndex) throws Exception {
        renditionName = renditionName.isEmpty()? getRenditionName(componentResource) : renditionName;
        String imagePath = BLANK;
        if(null != componentResource){
            if(ImageUtils.hasContent(componentResource, assetIndex)) {
                String fileReference;
                if (assetIndex == -1) {
                    fileReference = ResourceUtils.getPropertyAsString(componentResource, FILE_REFERENCE);
                } else {
                    List<String> fileReferences = ResourceUtils.getPropertyAsStrings(componentResource, FILE_REFERENCES);
                    fileReference = fileReferences.get(assetIndex);
                }
                String fileName = Text.getName(fileReference);
                if (StringUtils.isNotEmpty(renditionName)) {
                    Resource damAssetResource = componentResource.getResourceResolver().getResource(fileReference);
                    if(ImageUtils.isImage(damAssetResource)) {
                        imagePath = buildAssetPath(componentResource.getPath(), renditionName, fileName, assetIndex);
                    } else {
                        imagePath = buildAssetPath(componentResource.getPath(), fileName, assetIndex);
                    }
                } else {
                    imagePath = buildAssetPath(componentResource.getPath(), fileName, assetIndex);
                }
            }
        }
        return imagePath;
    }

    /**
     * Get asset path
     *
     * @param resource The resource to retrieve the asset path from
     * @param renditionName The name of the redition node
     * @param fileName The name of the node (file name)
     * @param assetIndex The position of the node under the resource
     * @return assetPath The asset path
     */
    public String buildAssetPath(Resource resource, String renditionName, String fileName, int assetIndex) {
        return (null == resource)? BLANK : buildAssetPath(resource.getPath(), renditionName, fileName, assetIndex);
    }

    /**
     * Get asset path
     *
     * @param resourcePath The path of the resource to fetch the asset path from
     * @param fileName The name of the node (file name)
     * @return assetPath The asset path
     */
    public String buildAssetPath(String resourcePath, String fileName) {
        return buildAssetPath(resourcePath, BLANK, fileName, -1);
    }

    /**
     * Get asset path
     *
     * @param resourcePath The path of the resource to fetch the asset path from
     * @param fileName The name of the node (file name)
     * @param assetIndex The position of the node under the resource
     * @return assetPath The asset path
     */
    public String buildAssetPath(String resourcePath, String fileName, int assetIndex) {
        return buildAssetPath(resourcePath, BLANK, fileName, assetIndex);
    }

    /**
     * Get asset path
     *
     * @param resourcePath The path of the resource to fetch the asset path from
     * @param renditionName The name of the rendition node
     * @param fileName The name of the node (file name)
     * @return assetPath The asset path
     */
    public String buildAssetPath(String resourcePath, String renditionName, String fileName) {
        return buildAssetPath(resourcePath, renditionName, fileName, -1);
    }

    /**
     * Get asset path
     *
     * @param resourcePath The path of the resource to fetch the asset path from
     * @param renditionName The name of the rendition node
     * @param fileName The name of the node (file name)
     * @param assetIndex The position of the node under the resource
     * @return assetPath The asset path
     */
    public String buildAssetPath(String resourcePath, String renditionName, String fileName, int assetIndex) {
        StringBuilder imagePath = new StringBuilder();
        if(!resourcePath.isEmpty()) {
            imagePath.append(resourcePath)
                    .append(DOT)
                    .append(ASSET_SELECTOR)
                    .append(DOT)
                    .append(assetIndex == -1? BLANK : assetIndex)
                    .append(assetIndex == -1? BLANK : DOT)
                    .append(ASSET_EXTENSION)
                    .append(renditionName.isEmpty()? BLANK : SLASH)
                    .append(renditionName.isEmpty()? BLANK : renditionName)
                    .append(fileName.isEmpty()? BLANK : SLASH)
                    .append(fileName);
        }
        return imagePath.toString();
    }

    /**
     * Get default rendition name
     *
     * @param resource The resource to fetch the rendition's default name
     * @return defaultRenditionName The default rendition name under the resource
     * @throws Exception
     */
    public String getDefaultRenditionName(Resource resource) throws Exception {
        String defaultRenditionName = "";
        if(null != configurationProvider){
            Configuration configuration = configurationProvider.getFor(resource.getResourceType());
            defaultRenditionName = configuration.asString(resource.getName() + DOT + DEFAULT_RENDITION_NAME);
            if(StringUtils.isEmpty(defaultRenditionName)) {
                defaultRenditionName = configuration.asString(DEFAULT_RENDITION_NAME);
            }
        }
        return defaultRenditionName;
    }

    /**
     * Get a collection of available rendition under a resource
     *
     * @param resource The resource to fetch the collection of renditions from
     * @return renditionCollection The collection of the renditions under the given resource
     * @throws Exception
     */
    public Collection<String> getAvailableRenditions(Resource resource) throws Exception {
        Configuration configuration = configurationProvider.getFor(resource.getResourceType());
        return configuration.asStrings(AVAILABLE_RENDITIONS);
    }

    /**
     * Get rendition name
     *
     * @param resource The resource to fetch the rendition's name from
     * @return renditionName The name of the rendition found under the given resource
     * @throws Exception
     */
    public String getRenditionName(Resource resource) throws Exception {
        String imageRendition = ResourceUtils.getPropertyAsString(resource, IMAGE_RENDITION_PROPERTY_NAME);
        String renditionName = getDefaultRenditionName(resource);
        if(null != imageRendition && getAvailableRenditions(resource).contains(imageRendition)) {
            renditionName = imageRendition;
        }
        return renditionName;
    }
}
