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

package danta.aem.contextprocessors.domprocesors;

import danta.aem.contextprocessors.AddStylingContextProcessor;
import danta.api.DOMProcessor;
import danta.api.ExecutionContext;
import danta.api.exceptions.ProcessException;
//import org.apache.felix.scr.annotations.Component;
//import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.annotations.Component;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static danta.Constants.HIGHEST_PRIORITY;

/**
 * This DOMProcessor works with the {@link AddStylingContextProcessor} to allow the developer to add custom CSS
 * classes to the container div.
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2014-09-04
 */
@Component(service = DOMProcessor.class)
public class XKSectionStylesDOMProcessor
        extends AbstractDOMProcessor {

    private static final  String XK_SECTION_ATTR = "data-xk-section";
    private static final  String XK_SECTION_STYLES_ATTR = "data-xk-section-styles";

    /**
     * Returns the DOMProcessor priority, which is HIGHEST_PRIORITY (See {@link danta.Constants}).
     * @return  HIGHEST_PRIORITY (See {@link danta.Constants}).
     */
    @Override
    public int priority() {
        return HIGHEST_PRIORITY;
    }

    @Override
    public void process(final ExecutionContext executionContext, final Document document)
            throws ProcessException {
        try {
            Elements xkSections = document.getElementsByAttribute(XK_SECTION_ATTR);
            if (xkSections != null) {
                for (Element xkSectionTag : xkSections) {
                    Element xkSectionStylesTag = xkSectionTag.getElementsByAttribute(XK_SECTION_STYLES_ATTR).first();
                    if (xkSectionStylesTag != null) {
                        String xkSectionStyles = xkSectionStylesTag.attr(XK_SECTION_STYLES_ATTR);
                        xkSectionTag.addClass(xkSectionStyles.trim());
                        xkSectionStylesTag.remove();
                    }
                }
            }
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }
}
