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

package layerx.aem.contextprocessors;

import com.google.common.collect.Sets;
import layerx.aem.templating.TemplateContentModelImpl;
import layerx.api.ExecutionContext;
import layerx.api.configuration.Configuration;
import layerx.api.configuration.Mode;
import layerx.api.exceptions.ProcessException;
import layerx.core.contextprocessors.AbstractCheckComponentCategoryContextProcessor;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static layerx.Constants.*;
import static layerx.aem.Constants.SLING_HTTP_REQUEST;
import static layerx.core.Constants.*;

/**
 * This context processor allows the developer to add custom css classes to the container div,
 * it is recommended to use this instead of overriding the "cq:htmlTag" node. (Using an override will remove the
 * default classes used by LayerX).
 * <p>
 * You can add a custom class adding the "xk_containerClasses" "Multi String" property to the xk.config node in your
 * component.
 * <p>
 * For example,
 * <pre><blockquote>
 * Property name: xk_containerClasses, Type: String[],  Value: xb-title-example, my-custom-css-class.
 * </blockquote></pre>
 * <p>
 * Also this context processor can add the "xk-placeholder" css class, which is used to trigger a full page refresh
 * after an author edition, based on the absence of a "trigger property" (or group of properties).
 * This css class triggers a page refresh in wcm mode after a content author updates the component content,
 * if the JCR properties defined as triggers are not present. This can be enabled by adding a string array property
 * named "xk_placeholderTriggers", with the properties which absence will trigger a full page refresh.
 * <p>
 * For example,
 * <pre><blockquote>
 * Property name: xk_placeholderTriggers, Type: String[],  Value: content.text, content.title
 * </blockquote></pre>
 * <p>
 *
 * xk_containerClasses     Multi-value string property with the css classes that must added to the component.
 *                         (it must be added to the xk.config node).
 * xk_placeholderTriggers  Multi-value string property with the properties which absence will trigger a full
 *                         page refresh (it must be added to the xk.config node).
 *
 * @see         /apps/xumakbase/components/section/base/cq:editConfig
 * @see         /apps/xpress/components/section/richtext/xk.config
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2014-09-04
 */
@Component
@Service
public class AddStylingContextProcessor
        extends AbstractCheckComponentCategoryContextProcessor<TemplateContentModelImpl> {

    @Override
    public Set<String> anyOf() {
        return Sets.newHashSet(STYLING_CATEGORY);
    }

    @Override
    public int priority() {
        return LOW_PRIORITY;
    }

    @Override
    public void process(final ExecutionContext executionContext, final TemplateContentModelImpl contentModel)
            throws ProcessException {
        try {
            SlingHttpServletRequest request = (SlingHttpServletRequest) executionContext.get(SLING_HTTP_REQUEST);
            Configuration config = configurationProvider.getFor(request.getResource().getResourceType());
            Map<String, Object> styling = new HashMap<>();
            Collection<String> containerClasses = config.asStrings(XK_CONTAINER_CLASSES_CP, Mode.MERGE);
            if(contentModel.has(PAGE_PROPERTIES_KEY) && contentModel.getAs(PAGE_PROPERTIES_KEY + "." + IS_EDIT_OR_DESIGN_MODE, Boolean.class)) {
                Collection<String> triggers = config.asStrings(XK_PLACEHOLDER_TRIGGERS_CP, Mode.SHALLOW);
                if (triggers != null && !triggers.isEmpty()) {
                    boolean displayPlaceholder = true;
                    for (String triggerProp : triggers) {
                        String propVal = contentModel.getAsString(triggerProp);
                        if (StringUtils.isNotEmpty(propVal)) {
                            displayPlaceholder = false;
                            break;
                        }
                    }
                    if (displayPlaceholder) {
                        styling.put(DISPLAY_PLACEHOLDER_STYLING_PROPERTY_NAME, DISPLAY_PLACEHOLDER_CSSN);
                        containerClasses.add(DISPLAY_PLACEHOLDER_CSSN);
                    }
                }
            }
            styling.put(CONTAINER_CLASSES_CP, containerClasses);
            StringBuffer stylingString = new StringBuffer();
            for (String className : containerClasses) {
                stylingString.append(className).append(SPACE);
            }
            styling.put(CONTAINER_STYLING_CSSN_AV, stylingString.toString());
            contentModel.set(STYLING_PROPERTIES_KEY, styling);

        } catch (Exception e) {
            throw new ProcessException(e);
        }

    }
}