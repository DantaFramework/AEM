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
import danta.api.TemplateContentModel;
import danta.core.commons.collections.POJOBackedMap;
import net.minidev.json.JSONObject;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.*;

import static danta.Constants.BLANK;
import static danta.aem.Constants.SLING_HTTP_REQUEST;
import static danta.core.util.ObjectUtils.wrap;

/**
 * Template Content Model Implementer
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2013-11-08
 */
public class TemplateContentModelImpl
        implements TemplateContentModel {

    private volatile Context currentContext;
    private final Context rootContext;
    private final SlingHttpServletRequest request;
    private final SlingHttpServletResponse response;

    public TemplateContentModelImpl(final SlingHttpServletRequest request, final SlingHttpServletResponse response) {
        this(request, response, new JSONObject());
    }

    public TemplateContentModelImpl(final SlingHttpServletRequest request, final SlingHttpServletResponse response, final Map<String, Object> initialModelData) {
        this.request = request;
        this.response = response;
        currentContext = rootContext = Context.newBuilder((initialModelData == null) ? new JSONObject() : wrap(initialModelData)).build();
        rootContext.data(SLING_HTTP_REQUEST, request);
    }

    protected final TemplateContentModelImpl newInstance(final SlingHttpServletRequest request, final SlingHttpServletResponse response) {
        return new TemplateContentModelImpl(request, response);
    }

    private Context newChildContext() {
        return Context.newBuilder(currentContext, new JSONObject()).build();
    }

    Context rootContext() {
        return rootContext;
    }

    JSONObject currentScopeData() {
        return scopeDataFor(currentContext);
    }

    JSONObject scopeDataFor(Context context) {
        return (JSONObject) context.model();
    }

    /**
     * @return
     */
    synchronized TemplateContentModelImpl extendScope(final Map<String, Object> isolatedModelData) {
        return extendScope().isolateToCurrentScope(wrap(isolatedModelData));
    }

    /**
     * @return
     */
    synchronized TemplateContentModelImpl extendScope() {
        currentContext = newChildContext();
        invalidateJSONString();
        return this;
    }

    /**
     * @return
     */
    synchronized TemplateContentModelImpl isolateScope() {
        currentContext = newChildContext(); // TODO: Flatten Model Hierarchy
        invalidateJSONString();
        return this;
    }

    /**
     * @return
     */
    synchronized TemplateContentModelImpl retractScope() {
        if (currentContext != rootContext) {
            Context oldContext = currentContext;
            currentContext = currentContext.parent();
            oldContext.destroy();
        }
        invalidateJSONString();
        return this;
    }

    TemplateContentModelImpl isolateToCurrentScope(final Map<String, Object> isolatedModelData) {
        currentScopeData().merge(wrap(isolatedModelData));
        invalidateJSONString();
        return this;
    }

    private TemplateContentModelImpl set(final Context context, final String path, final Object value) {
        List<String> keys = parsePath(path);
        StringBuilder builtPath = new StringBuilder();
        Map<String, Object> modelDataObj = scopeDataFor(context);
        List<String> ancestors = ancestors(keys);
        for (int i = 0; i < ancestors.size(); i++) {
            String key = ancestors.get(i);
            if (i > 0) builtPath.append(".");
            builtPath.append(key);
            Object valueObj = get(builtPath.toString());
            if (valueObj == null) { // Create the value at key.
                valueObj = new JSONObject();
                modelDataObj.put(key, valueObj);
            } else
            if (valueObj instanceof Map) {
                // Perfect... leave it then...
            } else {
                valueObj = new JSONObject();
                modelDataObj.put(key, valueObj); // Replace the value at key.
            }
            modelDataObj = (Map<String, Object>) valueObj;
        }

        modelDataObj.put(targetKey(keys), isValid(value) ? value : POJOBackedMap.toMap(value));
        invalidateJSONString();
        return this;
    }

    private boolean isValid(final Object o) {
        return (o instanceof String || o instanceof Number || o instanceof Date || o instanceof Calendar ||
                o instanceof Collection || o instanceof Map || o instanceof Boolean);
    }

    public enum ScopeLocality {
        ROOT,
        CLOSEST,
        ISOLATED
    }

    public TemplateContentModelImpl set(final String path, final Object value, final ScopeLocality locality) {
        switch (locality) {
            case ROOT:
                return set(rootContext(), path, value);
            case ISOLATED:
                List<String> parsedPath = parsePath(path);
                JSONObject isolatedModelData = new JSONObject();
                for (String name : ancestors(parsedPath)) {
                    isolatedModelData.put(name, new JSONObject());
                }
                isolatedModelData.put(targetKey(parsedPath), value);
                return isolateToCurrentScope(isolatedModelData);
            case CLOSEST:
            default:
                return set(currentContext, path, value);
        }
    }

    /**
     * Set Property name to value. This has two different effects depending on the state of the current Scope in the
     * ContentModel. If it's a normal member of the Scope hierarchy, then an attempt is made to find if any ancestor of
     * the key is already somewhere in the Scope hierarchy. This continues by back tracing the key until either one is
     * found, or the root Scope is reached with no existing ancestor. The key and any missing intermediates then are
     * created on the closest Scope found, or the current local one if the entire key structure is unique.
     * <p/>
     * For example, consider a key like <code>lists.GoT.Starks.killed</code> and value of "Eddard".
     *
     * @param path
     * @param value
     *
     * @return
     */

    @Override
    public TemplateContentModelImpl set(final String path, final Object value) {
        return set(path, value, ScopeLocality.CLOSEST);
    }

    public TemplateContentModelImpl setAsIsolated(final String path, final Object value) {
        return set(path, value, ScopeLocality.ISOLATED);
    }

    /**
     * @param name
     * @param value
     *
     * @return
     */
    public TemplateContentModelImpl setToRoot(final String name, final Object value) {
        return set(name, value, ScopeLocality.ROOT);
    }

    private List<String> parsePath(final String path) {
        StringTokenizer tokenizer = new StringTokenizer(path, "./");
        int len = tokenizer.countTokens();
        if (len == 1) {
            return Arrays.asList(path);
        }
        List<String> keys = new ArrayList<>(len);
        while (tokenizer.hasMoreTokens()) {
            keys.add(tokenizer.nextToken());
        }
        return keys;
    }

    /**
     * Set Attribute name to value. Attributes are not included in static representations of the ContentModel.
     * For example, a JSON representation provided to clients. They are also not included in any list of model keys or
     * values, and can only be retrieved by using their explicit key using getAttribute(), or using the standard get()
     * with @ prepended to the key name. This makes them useful for sharing information between ContextProcessors
     * without worrying that it might be accidentally sent to clients or casually provided publicly as part of a key
     * Set.
     * <p/>
     * There's an important difference between attributes and properties regarding Scopes. Unlike data Properties, they
     * are not scoped, and will persist even if all Scopes (up to the root, of course) have been destroyed.
     * <p/>
     *
     * @param name
     *         The name of the Attribute
     * @param value
     *         The value of the Attribute
     *
     * @return This instances of the TemplateContentModelImpl to allow chaining
     */
    @Override
    public TemplateContentModelImpl setAttribute(final String name, final Object value) {
        currentContext.data(name, value);
        return this;
    }

    /**
     * @param name
     *
     * @return The Value of the Attribute as an Object
     */
    @Override
    public Object getAttribute(final String name) {
        return currentContext.data(name);
    }

    private String targetKey(final List<String> pathParts) {
        return pathParts.get(pathParts.size() - 1);
    }

    private List<String> ancestors(final List<String> pathParts) {
        return (pathParts.size() > 1) ? pathParts.subList(0, pathParts.size() - 1) : Collections.EMPTY_LIST;
    }

    @Override
    public String getAsString(final String name) {
        Object value = get(name);
        return (value != null) ? value.toString() : BLANK;
    }

    @Override
    public Object get(final String name) {
        return currentContext.get(name);
    }

    @Override
    public <T> T getAs(final String name, final Class<T> type)
            throws Exception {
        return (is(name, type)) ? (T) currentContext.get(name) : null;
    }

    @Override
    public boolean has(final String name) {
        return (name != null && !name.isEmpty() && currentContext.get(name) != null);
    }

    @Override
    public <T> boolean is(String name, Class<T> type) {
        return (has(name) && type != null && type.isAssignableFrom(get(name).getClass()));
    }

    Context handlebarsContext() {
        return currentContext;
    }

    public SlingHttpServletRequest request()
            throws Exception {
        return (has(SLING_HTTP_REQUEST)) ? getAs(SLING_HTTP_REQUEST, SlingHttpServletRequest.class) : null;
    }

    final HttpServletResponse response()
            throws Exception {
        return response;
    }

    final HttpServletResponse wrappedResponse()
            throws Exception {
        //return new TemplatingSupportFilter.CharResponseWrapper(response());
        return new CharResponseWrapper(response());
    }

    @Override
    public JSONObject toJSONObject(String... keys) {
        JSONObject modelDataObj = new JSONObject();
        for (String key : keys) {
            Object value = get(key);
            if (value != null)
                modelDataObj.put(key, value);
        }
        return modelDataObj;
    }

    public JSONObject toJSONObject() {
        synchronized (currentScopeData()) {
            JSONObject modelDataObj = wrap(currentScopeData());
            Context context = currentContext.parent();
            while (context != null) {
                modelDataObj.merge(context.model());
                context = context.parent();
            }
            return modelDataObj;
        }
    }

    private void invalidateJSONString() {
        cachedJSONString = null;
    }

    private volatile String cachedJSONString = null;

    public String toJSONString() {
        if (cachedJSONString == null) {
            cachedJSONString = toJSONObject().toJSONString();
        }
        //return cachedJSONString;
        return toJSONObject().toString();
    }

    @Override
    public String toString() {
        return toJSONString();
    }

    static final class CharResponseWrapper
            extends HttpServletResponseWrapper {

        private CharArrayWriter output;

        public CharResponseWrapper(HttpServletResponse response) {
            super(response);
            output = new CharArrayWriter();
        }

        public String toString() {
            return output.toString();
        }

        public PrintWriter getWriter() {
            return new PrintWriter(output);
        }
    }
}
