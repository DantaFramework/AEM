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

import com.day.cq.wcm.api.components.ComponentManager;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.google.common.collect.Sets;
import danta.api.ContextProcessorEngine;
import danta.api.DOMProcessorEngine;
import danta.api.configuration.Configuration;
import danta.api.configuration.ConfigurationProvider;
import danta.api.configuration.Mode;
import danta.core.execution.ExecutionContextImpl;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.*;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.scripting.api.AbstractSlingScriptEngine;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static danta.Constants.*;
import static danta.aem.Constants.SLING_HTTP_REQUEST;
import static danta.core.Constants.XK_COMPONENT_CATEGORY;

/**
 * Danta script engine
 *
 * @author      jbarrera
 * @version     1.0.0
 * @since       2016-07-27
 */
public class DantaScriptEngine
        extends AbstractSlingScriptEngine {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String CONFIG_SERVICE = "config-service";

    private ConfigurationProvider configurationProvider;
    private ResourceResolverFactory resourceResolverFactory;
    private ContextProcessorEngine contextProcessorEngine;
    private DOMProcessorEngine domProcessorEngine;
    private HelperFunctionBind helperFunctionBind;

    public DantaScriptEngine(DantaScriptEngineFactory layerXScriptEngineFactory,
                              ContextProcessorEngine contextProcessorEngine,
                              ConfigurationProvider configurationProvider,
                              ResourceResolverFactory resourceResolverFactory,
                              DOMProcessorEngine domProcessorEngine,
                              HelperFunctionBind helperFunctionBind) {
        super(layerXScriptEngineFactory);
        this.configurationProvider = configurationProvider;
        this.resourceResolverFactory = resourceResolverFactory;
        this.contextProcessorEngine = contextProcessorEngine;
        this.domProcessorEngine = domProcessorEngine;
        this.helperFunctionBind = helperFunctionBind;
    }

    public Object eval(Reader reader, ScriptContext scriptContext) throws ScriptException {
        Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
        SlingBindings slingBindings = new SlingBindings();
        slingBindings.putAll(bindings);
        ResourceResolver adminResourceResolver = null;

        try {
            SlingHttpServletRequest request = slingBindings.getRequest();
            SlingHttpServletResponse response = slingBindings.getResponse();
            Resource resource = slingBindings.getResource();

            if (request.getMethod().equalsIgnoreCase(HTTP_GET) && configurationProvider.hasConfig(resource.getResourceType()) && !isUnstructuredResource(resource)) {
                String requestedResourcePath = resource.getPath();
                Map<String, Object> authenticationInfo = new HashMap<String, Object>();
                authenticationInfo.put(ResourceResolverFactory.SUBSERVICE, CONFIG_SERVICE);
                adminResourceResolver = resourceResolverFactory.getServiceResourceResolver(authenticationInfo);

                // Uplevel the privileges so we're sure we can access Templates
                if (!ResourceUtil.isSyntheticResource(resource)) {
                    resource = adminResourceResolver.getResource(requestedResourcePath);
                } else {
                    Resource syntheticResource = new IncludeHelperSyntheticResource(adminResourceResolver,
                            resource.getPath(),
                            resource.getResourceType());
                    resource = syntheticResource;
                }

                List<HelperFunction> helpers = helperFunctionBind.getHelpers();

                Handlebars handlebars = new Handlebars(new HTMLResourceBasedTemplateLoader(resource));
                handlebars.infiniteLoops(true);
                for (HelperFunction helper : helpers) {
                    handlebars.setStartDelimiter(START_DELIM);
                    handlebars.setEndDelimiter(END_DELIM);
                    handlebars.registerHelper(helper.name(), helper);
                }

                TemplateContentModelImpl contentModel = (TemplateContentModelImpl) request.getAttribute(TEMPLATE_CONTENT_MODEL_ATTR_NAME);
                if (contentModel == null) {
                    contentModel = new TemplateContentModelImpl(request, response);
                    request.setAttribute(TEMPLATE_CONTENT_MODEL_ATTR_NAME, contentModel);
                } else {
                    contentModel.extendScope();
                }

                ExecutionContextImpl executionContext = new ExecutionContextImpl();
                executionContext.put(SLING_HTTP_REQUEST, request);
                executionContext.put(ENGINE_RESOURCE, resource.getResourceType());

                List<String> currentProcessorChain = contextProcessorEngine.execute(executionContext, contentModel);

                Map<String, Object> statisticsMap = new HashMap<>();
                statisticsMap.put(PROCESSORS, currentProcessorChain);
                contentModel.set(STATISTICS_KEY, statisticsMap); //Add to ContentModel for later inspection by Components

                DantaTemplateSourceReader templateSourceReader = new DantaTemplateSourceReader();
                String unprocessedResponse = templateSourceReader.contentReader(reader);
                String outputHTML = unprocessedResponse;

                Template template = handlebars.compileInline(unprocessedResponse);
                Context handlebarsContext = contentModel.handlebarsContext();
                outputHTML = template.apply(handlebarsContext);

                if (hasPageAndHTMLPageCategories(adminResourceResolver, resource)) {
                    Document document = Jsoup.parse(outputHTML);
                    domProcessorEngine.execute(executionContext, document);
                    outputHTML = document.html();
                }

                contentModel.retractScope();
                PrintWriter out = response.getWriter();
                out.write(outputHTML);

                if (adminResourceResolver != null) {
                    adminResourceResolver.close();
                }

            } else {
                log.debug("{} is not a Danta component", resource.getPath());
            }

        } catch (RuntimeException re) {
            throw re;
        } catch (Exception ew) {
            throw new ScriptException(ew);
        }

        return null;
    }

    /**
     * This method validate if the component has or inherits the categories 'page' and 'htmlpage' in the xk.config node.
     *
     * @param resolver
     * @param resource
     */
    private boolean hasPageAndHTMLPageCategories(ResourceResolver resolver, Resource resource)
            throws Exception {

        boolean hasPageAndHTMLPageCategories = false;
        if (resource != null) {
            ComponentManager componentManager = resolver.adaptTo(ComponentManager.class);
            com.day.cq.wcm.api.components.Component component = componentManager.getComponentOfResource(resource);

            if (component != null) {
                Configuration configuration = configurationProvider.getFor(resource.getResourceType());
                Collection<String> compCategories = configuration.asStrings(XK_COMPONENT_CATEGORY, Mode.MERGE);

                if (compCategories.containsAll(Sets.newHashSet(PAGE_CATEGORY, HTML_PAGE_CATEGORY))) {
                    hasPageAndHTMLPageCategories = true;
                }
            }
        }
        return hasPageAndHTMLPageCategories;
    }

    /**
     * This method validate if a resource is not a container of other component included directly into the template.
     *
     * @param resource
     */
    private boolean isUnstructuredResource(Resource resource)
            throws Exception {

        // Validate if it is a unstructured resource
        Boolean isUnstructuredResource = false;
            if (resource instanceof ResourceWrapper) {
                ResourceWrapper resourceWrapper = (ResourceWrapper) resource;
                Resource innerResource = resourceWrapper.getResource();
                if (innerResource.isResourceType("nt:unstructured")) {
                    log.error("{} Invalid resource type, not allowed the use of components as containers directly included into the template. ", resource.getPath());
                    isUnstructuredResource = true;
                }
            }
        return isUnstructuredResource;
    }

}
