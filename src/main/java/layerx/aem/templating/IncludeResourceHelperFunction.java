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

package layerx.aem.templating;

import com.day.cq.wcm.api.components.ComponentManager;
import com.github.jknack.handlebars.Handlebars;
import layerx.api.configuration.ConfigurationProvider;
import layerx.core.util.UrlUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static layerx.Constants.*;

/**
 * The include helper allows the developer to include components in the html content.
 * It can be used to include foundation or custom components. It is a replacement for the <cq:include /> tag
 * <p>
 * Examples:
 * <pre><blockquote>
 *      {%#include "exampleComp" resourceType="myCompany/components/section/titleexample"%}{%/include%}
 * </blockquote></pre>
 * <pre><blockquote>
 *      {%#include "examplePar" resourceType="foundation/components/parsys"%}{%/include%}
 * </blockquote></pre>
 * <pre><blockquote>
 *      {%#include "examplePar" resourceType="foundation/components/parsys" prefix="ColumnOne"%}{%/include%}
 * </blockquote></pre>
 * <p>
 * The first argument is a name. the second parameter is the relative path of the component.
 *
 * name:   a name to the component included.
 *
 * resourceType: the resourceType that will be used for the component included. Always use a relative path.
 *
 * prefix: an optional prefix, which can be the 'this' variable, a handlebars
 *         variable, or a static string; that will be prepended to the variable
 *         provided in the 'name' attribute.
 *
 * suffix: An optional postfix, which can be the 'this' variable, a handlebars
 *         variable, or a static string; that will be appended to the variable
 *         provided in the 'name' attribute.
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2013-11-09
 */
@Component(label = "Include Resource Helper Function Configuration", metatype = true)
@Service
@Properties({
        @Property(name = IncludeResourceHelperFunction.CONFIG_CHARACTERS_PROPERTY,
                value = IncludeResourceHelperFunction.DEFAULT_INVALID_CHARACTERS,
                label="Invalid characteres",
                description = "Regular expression for invalid characters in the resource name. Default value: [ \\t$&+,:;=?@#|'<>.^*()%!~\\[\\]{}] "),
})
public class IncludeResourceHelperFunction
        extends AbstractAEMHelperFunction<String> {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY, policy = ReferencePolicy.STATIC)
    protected ConfigurationProvider configurationProvider;

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    static final String DEFAULT_INVALID_CHARACTERS = "[ \\t$&+,:;=?@#|'<>.^*()%!~\\[\\]{}]";
    static final String CONFIG_CHARACTERS_PROPERTY = "invalidCharacteres";
    private String configCharacters;

    public IncludeResourceHelperFunction() {
        super("include");
    }

    @Override
    public CharSequence execute(String path)
            throws Exception {
        String responseString = "";
        if (!isInvalidPath(path)) {
            TemplateContentModelImpl contentModel = contentModel();
            SlingHttpServletRequest request = contentModel.request();
            ResourceResolver resourceResolver = request.getResourceResolver();
            Resource resource = request.getResource();
            if (path != null) {
                path = interpolate(path);
                path = attachSuffixOrPrefix(path, (String) param("prefix"), (String) param("suffix"));
                if (!path.startsWith("/")) {
                    path = request.getResource().getPath() + "/" + path;
                }
                path = ResourceUtil.normalize(path);
            }
            ComponentManager componentManager = resourceResolver.adaptTo(ComponentManager.class);
            Resource tmp = request.getResourceResolver().resolve(path);
            String resourceType = interpolate((String) param("resourceType"));

            final RequestDispatcherOptions opts = new RequestDispatcherOptions();
            opts.setForceResourceType(resourceType);
            RequestDispatcher dispatcher;
            if (tmp != null) {
                if (tmp instanceof NonExistingResource) {
                    if (resourceType != null) {
                        resource = new IncludeHelperSyntheticResource(request.getResourceResolver(), path, resourceType);
                        opts.remove(RequestDispatcherOptions.OPT_FORCE_RESOURCE_TYPE);
                    }
                } else {
                    resource = tmp;
                }
            }
            com.day.cq.wcm.api.components.Component component = componentManager.getComponentOfResource(resource);

            dispatcher = request.getRequestDispatcher(resource, opts);
            HttpServletResponse wrappedResponse = contentModel().wrappedResponse();
            dispatcher.include(request, wrappedResponse);
            responseString = wrappedResponse.toString();
        } else {
            log.error("{} It is not valid", path);
        }
        return new Handlebars.SafeString(responseString);
    }

    private final String attachSuffixOrPrefix(String path, String prefix, String suffix) {

        boolean p = (prefix == null ? false : true);
        boolean s = (suffix == null ? false : true);

        String lastSegment = UrlUtils.getLastPathSegment(path);

        try {
            if (p || s) {
                if (p) {
                    lastSegment = interpolate(prefix) + DASH + lastSegment;
                }
                if (s) {
                    lastSegment += DASH + interpolate(suffix);
                }
            }
        } catch (IOException e) {
            log.error("Check interpolation of prefix or suffix parameters", e);
        }

        if (path.contains(SLASH)) {
            String[] segments = UrlUtils.getPathSegments(path);
            segments[segments.length - 1] = lastSegment;
            if (path.startsWith(SLASH)) {
                path = SLASH + StringUtils.join(segments, SLASH);
            } else {
                path = StringUtils.join(segments, SLASH);
            }
        } else {
            path = lastSegment;
        }

        return path;
    }

    private static final Handlebars handlebars;

    static {
        handlebars = new Handlebars();
        handlebars.setStartDelimiter(START_DELIM);
        handlebars.setEndDelimiter(END_DELIM);
    }

    protected final String interpolate(String argument)
            throws IOException {
        if (argument.contains(START_DELIM))
            argument = handlebars.compileInline(argument).apply(contentModel().handlebarsContext());
        return argument;
    }

    /**
     * This method validate if a resource name has a valid name, using a regular expression with the invalid characters.
     *
     * @param path
     */
    private final boolean isInvalidPath(String path) {
        boolean isInvalidPath = false;
        try {
            if (configCharacters.isEmpty()) {
                configCharacters = DEFAULT_INVALID_CHARACTERS;
            }
            Pattern pattern = Pattern.compile(configCharacters);
            Matcher matcher = pattern.matcher(path);
            isInvalidPath =  matcher.find();
        } catch (PatternSyntaxException e) {
            log.error("Pattern Syntax exception ", e);
        }
        return isInvalidPath;
    }

    @Activate
    public void activate(final ComponentContext context) throws Exception {
        this.configCharacters = OsgiUtil.toString(context.getProperties().get(CONFIG_CHARACTERS_PROPERTY), DEFAULT_INVALID_CHARACTERS);
    }
}
