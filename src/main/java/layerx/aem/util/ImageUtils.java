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

package layerx.aem.util;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.wcm.api.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.List;

import static layerx.Constants.*;
import static layerx.aem.Constants.TRANSFORM_SELECTOR;

/**
 * Container for the CQ general objects.
 *
 * @author      palecio
 * @version     1.0.0
 * @since       2014-03-27
 */
public class ImageUtils {

    private ImageUtils(){}

    @Deprecated
    public static String getImagePath(Resource resource, String renditionName){
        String imagePath = "";
        if(null != resource){
            if(hasContent(resource)){
                if (StringUtils.isNotEmpty(renditionName)) {
                    imagePath = getTransformedImagePath(resource, renditionName);
                } else {
                    imagePath = getSrc(resource);
                }
            }
        }
        return imagePath;
    }

    @Deprecated
    public static String getSrc(Resource imageResource) {
        return imageResource.getPath() + IMG_EXT + PNG_EXT;
    }

    /**
     * Looks for a fileReference property on the resource sent as argument and returns the DAM resource referenced.
     * @param resource a resource with a fileReference property.
     * @return the DAM asset resource referenced or null if the path stored in fileReference is invalid or the
     * fileReference property doesn't exist.
     */
    public static Resource getDamAssetResource(Resource resource) {
        Resource damAssetResource = null;
        if(null != resource) {
            ResourceResolver resourceResolver = resource.getResourceResolver();
            String fileReference = ResourceUtils.getPropertyAsString(resource, FILE_REFERENCE);
            if(StringUtils.isNotEmpty(fileReference)) {
                damAssetResource = resourceResolver.getResource(fileReference);
            }
        }
        return damAssetResource;
    }

    public static boolean hasContent(Resource resource) {
        return hasContent(resource, -1);
    }

    public static boolean hasContent(Resource resource, int imageIndex) {
        boolean hasContent = false;
        if(null != resource) {
            ResourceResolver resourceResolver = resource.getResourceResolver();
            String fileReference = BLANK;
            if(imageIndex == -1) {
                fileReference = ResourceUtils.getPropertyAsString(resource, FILE_REFERENCE);
            } else {
                List<String> fileReferences = ResourceUtils.getPropertyAsStrings(resource, FILE_REFERENCES);
                if (fileReferences.size() > imageIndex) {
                    fileReference = fileReferences.get(imageIndex);
                }
            }
            if(StringUtils.isNotEmpty(fileReference)) {
                Resource imageResource = resourceResolver.getResource(fileReference);
                if (null != imageResource) {
                    hasContent = true;
                }
            }
        }
        return hasContent;
    }

    public static boolean isImage(Resource resource) {
        if (null != resource) {
            Asset asset = DamUtil.resolveToAsset(resource);
            if (null != asset) {
                String mimeType = asset.getMimeType();
                return StringUtils.containsIgnoreCase(mimeType, IMAGE);
            }
        }
        return false;
    }

    @Deprecated
    public static String getImagePath(Resource resource){
        return getImagePath(resource, "");
    }

    @Deprecated
    public static String getTransformedImagePath(Resource resource, String renditionName) {
        return resource.getPath() + TRANSFORM_SELECTOR + "/" + renditionName + "/" + IMAGE + JPG_EXT;
    }

    @Deprecated
    public static String getPageImagePath(Page page, String renditionImage){
        String pageImagePath = "";
        Resource imageResource = page.getContentResource(IMAGE);
        if(null != imageResource){
            pageImagePath = ImageUtils.getImagePath(imageResource, renditionImage);
        }
        return pageImagePath;
    }

}
