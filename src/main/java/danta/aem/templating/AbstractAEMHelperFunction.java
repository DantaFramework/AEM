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

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Options;
import org.apache.felix.scr.annotations.Component;
import org.apache.sling.api.SlingHttpServletRequest;

import java.io.IOException;
import java.util.Map;

import static danta.Constants.TEMPLATE_CONTENT_MODEL_ATTR_NAME;
import static danta.aem.Constants.SLING_HTTP_REQUEST;

/**
 * The abstraction for AEM Helper function
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2013-11-06
 */
@Component (componentAbstract = true)
public abstract class AbstractAEMHelperFunction<T>
        implements HelperFunction<T> {

    private static final ThreadLocal<Options> threadLocal = new ThreadLocal<>();
    private final String name;

    protected AbstractAEMHelperFunction(String name) {
        this.name = name;
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public abstract CharSequence execute(final T valueObj)
            throws Exception;

    protected final CharSequence transclude(final Object valueObj)
            throws Exception {
        return options().fn(valueObj);
    }

    protected final CharSequence transclude()
            throws Exception {
        return options().fn();
    }

    protected final Options options() {
        return threadLocal.get();
    }

    protected final Object paramAt(final int index) {
        return options().param(index);
    }

    protected final <T> T paramAt(final int index, final T defaultValue) {
        return options().param(index);
    }

    protected final Object[] params() {
        return options().params;
    }

    protected final Map<String, Object> paramsMap() {
        return options().hash;
    }

    protected final Object param(String name) {
        return options().hash(name);
    }

    protected final <T> T param(String name, final T defaultValue) {
        return options().hash(name);
    }

    protected final SlingHttpServletRequest request() {
        Options options = options();
        if (options != null) {
            Context context = options.context;
            SlingHttpServletRequest result = (SlingHttpServletRequest) context.get(SLING_HTTP_REQUEST);
            return result;
        }
        return null;
    }

    protected final TemplateContentModelImpl contentModel() {
        return request() != null? (TemplateContentModelImpl) request().getAttribute(TEMPLATE_CONTENT_MODEL_ATTR_NAME) : null;
    }

    @Override
    public final CharSequence apply(final T value, final Options options)
            throws IOException {
        try {
            threadLocal.set(options);
            return execute(value);
        } catch (Exception ew) {
            throw new RuntimeException(ew);
        } finally {
            threadLocal.remove();
        }
    }
}
