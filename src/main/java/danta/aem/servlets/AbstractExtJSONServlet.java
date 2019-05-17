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

import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * The abstraction for json extension servlet
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2012-11-15
 */
public abstract class AbstractExtJSONServlet
        extends SlingSafeMethodsServlet {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    @Reference
    protected SlingRepository repository;

    @Reference
    protected ResourceResolverFactory resourceResolverFactory;

    private ThreadLocal<JSONArray> threadLocal;

    protected final void addElement(String text, String value)
            throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("value", value);
        obj.put("text", text);
        threadLocal.get().put(obj);
    }

    protected final void addElement(String value)
            throws JSONException {
        addElement(value, value);
    }

    protected abstract void loadElements(SlingHttpServletRequest request)
            throws Exception;

    protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        try {
            threadLocal = new ThreadLocal<JSONArray>() {
                @Override
                protected JSONArray initialValue() {
                    return new JSONArray();
                }
            };
            loadElements(request);
            out.write(threadLocal.get().toString());
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            out.flush();
            threadLocal.remove();
        }
    }
}
