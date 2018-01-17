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

import danta.core.templating.AbstractTemplateContentModelImpl;
import net.minidev.json.JSONObject;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.*;

import static danta.aem.Constants.SLING_HTTP_REQUEST;

/**
 * Template Content Model Implementer
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2013-11-08
 */
public class TemplateContentModelImpl
        extends AbstractTemplateContentModelImpl {

    private final SlingHttpServletRequest request;
    private final SlingHttpServletResponse response;

    public TemplateContentModelImpl(final SlingHttpServletRequest request, final SlingHttpServletResponse response) {
        this(request, response, new JSONObject());
    }

    public TemplateContentModelImpl(final SlingHttpServletRequest request, final SlingHttpServletResponse response, final Map<String, Object> initialModelData) {
        super(initialModelData);
        getRootContext().data(SLING_HTTP_REQUEST, request);

        this.request = request;
        this.response = response;
    }

    public SlingHttpServletRequest request()
            throws Exception {
        return (has(SLING_HTTP_REQUEST)) ? getAs(SLING_HTTP_REQUEST, SlingHttpServletRequest.class) : null;
    }

    final HttpServletResponse response()
            throws Exception {
        return response;
    }
}