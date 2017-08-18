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

package layerx.aem.services;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;

/**
 * The content utils interface
 *
 * @author      jbarrera
 * @version     1.0.0
 * @since       2016-12-01
 */
public interface ContentUtils {

    /**
     *
     * @param path This is a resource/page/node path
     * @param request This is a SlignHttpServletRequest
     * @return outputAsBytes The rendered output of a resource in bytes
     */
    public byte[] getOutputAsBytes(String path, SlingHttpServletRequest request);

    /**
     *
     * @param path This is a resource/page/node path
     * @param resolver This is a resource resolver
     * @return outputAsBytes The rendered output of a resource in bytes
     */
    public byte[] getOutputAsBytes(String path, ResourceResolver resolver);

}
