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

import layerx.api.ContentModel;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

/**
 * The content model factory service interface
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2014-03-12
 */
public interface ContentModelFactoryService {

    /**
     * @param request This is a SlingHttpServletRequest
     * @param response This is a SlingHttpServletResponse
     * @return contentModel The Content Model of a resource
     */
    public ContentModel getContentModel(final SlingHttpServletRequest request, final SlingHttpServletResponse response);

}