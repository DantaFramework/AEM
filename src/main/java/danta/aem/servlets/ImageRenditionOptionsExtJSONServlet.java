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

package danta.aem.servlets;

import danta.api.configuration.Configuration;
import danta.api.configuration.ConfigurationProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.apache.sling.api.SlingHttpServletRequest;

import javax.servlet.Servlet;
import java.util.Collection;

import static danta.Constants.DOT;
import static danta.aem.Constants.AVAILABLE_RENDITIONS;

/**
 * This is a Image Rendition Options Ext JSON Servlet
 *
 * @author      palecio
 * @version     1.0.0
 * @since       2014-06-04
 */
@Component(service = Servlet.class)
public class ImageRenditionOptionsExtJSONServlet
        extends AbstractExtJSONServlet {

    public static final String OPTIONS = "options";
    public static final String IMAGE_RENDITIONS_SELECTORS = "renditions" + DOT + OPTIONS;

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
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
