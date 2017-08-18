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

package layerx.aem.assets;

import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.Resource;

import java.util.Collection;

/**
 * Asset Path Service
 *
 * @author      palecio
 * @version     1.0.0
 * @since       2014-09-22
 */
public interface AssetPathService {

    //image paths
    public String getComponentAssetPath(Resource componentResource) throws Exception;
    public String getComponentAssetPath(Resource componentResource, String renditionName) throws Exception;
    public String getComponentAssetPath(Resource componentResource, int imageIndex) throws Exception;
    public String getComponentAssetPath(Resource componentResource, String renditionName, int imageIndex) throws Exception;
    public String getComponentImagePath(Resource componentResource) throws Exception;
    public String getPageImagePath(Page page, Resource componentResource) throws Exception;

    //renditions
    public String getRenditionName(Resource resource) throws Exception;
    public String getDefaultRenditionName(Resource resource) throws Exception;
    public Collection<String> getAvailableRenditions(Resource resource) throws Exception;

    //path building
    public String buildAssetPath(String resourcePath, String transformName, String fileName, int imageIndex);
    public String buildAssetPath(Resource resource, String renditionName, String fileName, int imageIndex);
    public String buildAssetPath(String resourcePath, String fileName);
    public String buildAssetPath(String resourcePath, String fileName, int imageIndex);
    public String buildAssetPath(String resourcePath, String renditionName, String fileName);

}
