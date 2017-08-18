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

package layerx.aem.servlets;

import layerx.api.configuration.Configuration;
import layerx.api.configuration.ConfigurationProvider;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;

import java.util.Collection;

import static layerx.Constants.DOT;
import static layerx.Constants.JSON;
import static layerx.aem.Constants.AVAILABLE_RENDITIONS;

/**
 * This is a Image Rendition Options Ext JSON Servlet
 *
 * @author      palecio
 * @version     1.0.0
 * @since       2014-06-04
 */
@Component
@Service
@Properties({
        @Property(name = "service.description", value = "Image Rendition Options Ext JSON Servlet"),
        @Property(name = "sling.servlet.selectors", value = ImageRenditionOptionsExtJSONServlet.IMAGE_RENDITIONS_SELECTORS),
        @Property(name = "sling.servlet.extensions", value = JSON),
        @Property(name = "sling.servlet.resourceTypes", value = "sling/servlet/default")
})
public class ImageRenditionOptionsExtJSONServlet
        extends AbstractExtJSONServlet {

    public static final String OPTIONS = "options";
    public static final String IMAGE_RENDITIONS_SELECTORS = "renditions" + DOT + OPTIONS;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY, policy = ReferencePolicy.STATIC)
    protected ConfigurationProvider configurationProvider;

    @Override
    protected void loadElements(SlingHttpServletRequest request) throws Exception {
        Configuration configuration = configurationProvider.getFor(request.getResource().getResourceType());
        Collection<String> availableRenditions = configuration.asStrings(AVAILABLE_RENDITIONS);
        for (String availableRendition : availableRenditions) {
            addElement(availableRendition);
        }
    }
}
