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
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The abstraction for select HTML servlet
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2012-11-15
 */
public abstract class AbstractSelectHTMLServlet
        extends SlingSafeMethodsServlet {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Reference
    protected SlingRepository repository;

    @Reference
    protected ResourceResolverFactory resourceResolverFactory;

    private ThreadLocal<Map<String, String>> threadLocal;

    protected final void addElement(String text, String value)
            throws Exception {
        threadLocal.get().put(text, value);
    }

    protected final void addElement(String value)
            throws Exception {
        addElement(value, value);
    }

    protected abstract void loadElements(SlingHttpServletRequest request)
            throws Exception;

    protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        try {
            threadLocal = new ThreadLocal<Map<String, String>>() {
                @Override
                protected Map<String, String> initialValue() {
                    return new HashMap<String, String>();
                }
            };
            loadElements(request);
            Set<String> keys = threadLocal.get().keySet();
            for (String key : keys) {
                String value = threadLocal.get().get(key);
                out.write("<option value='" + value + "'>" + key + "</option>");
            }
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            out.flush();
            threadLocal.remove();
        }
    }
}
