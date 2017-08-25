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

package danta.aem.templating;

import com.github.jknack.handlebars.Handlebars;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import static danta.Constants.BLANK;

/**
 * Provides a way to test if a property in the context exists, and if not, displays the default value, if there is one.
 * <pre><blockquote>
 *     Example: {%#property "context.propertyName" defaultValue="Default value" %}{%/property%}
 * </blockquote></pre>
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2013-10-30
 */
@Component
@Service
public class PropertyHelperFunction
        extends AbstractAEMHelperFunction<String> {

    public PropertyHelperFunction() {
        super("property");
    }

    @Override
    public CharSequence execute(final String property)
            throws Exception {
        String defaultValue = (param("defaultValue") != null) ? (String) param("defaultValue") : BLANK;
        Object value = options().context.get(property);
        String responseString = (value != null) ? value.toString() : defaultValue;
        return new Handlebars.SafeString(responseString);
    }
}
