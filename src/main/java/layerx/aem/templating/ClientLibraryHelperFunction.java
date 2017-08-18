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

import com.adobe.granite.ui.clientlibs.HtmlLibraryManager;
import com.github.jknack.handlebars.Handlebars;
import layerx.aem.util.ClientLibraryUtil;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The clientLibrary Helper includes AEM html client libraries.
 * It can be used to include a js or a css library.
 * <p>
 * Examples:
 * <pre><blockquote>
 *     {%clientLibrary "cq.collab.comments" type="categories" cacheBuster="true" minify="true"%}
 * </blockquote></pre>
 * <p>
 * It also can include only the css or the javascript:
 * <pre><blockquote>
 *     {%clientLibrary "myproject.components, cq.collab.comments" type="js" cacheBuster="true" minify="false"%}
 *     {%clientLibrary "myproject.components" type="css" cacheBuster="false" minify="false" media="print"%}
 * </blockquote></pre>
 * <p>
 * The first argument must be a comma separated list with the client libraries
 * categories. The second parameter is the type of the library that must be
 * included, it can be:
 * <p>
 * <ul>
 * <li>categories: to include both css and javascript.</li>
 * <li>css: to include only the css.</li>
 * <li>js: to include only the javascript.</li>
 * </ul>
 * <p>
 * The cacheBuster parameter is used to add a timestamp to the url, in order to
 * prevent problems with the browsers cache during development.
 *
 * categoriesList    comma separated list with the client libraries categories.
 * type              library type that should be included, it can be:
 *                          [css | js | categories]
 * cacheBuster       bool value to activate the cache booster [true | false] (optional)
 * minify            bool value to get the minified version of the library.
 * media             String value to include media attribute
 *
 * @author      neozilon
 * @version     1.0.0
 * @since       2013-11-06
 */
@Component
@Service
public class ClientLibraryHelperFunction
        extends AbstractAEMHelperFunction<String> {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY, policy = ReferencePolicy.STATIC)
    HtmlLibraryManager htmlLibraryManager;

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_MINIFY = "minify";
    private static final String ATTRIBUTE_CACHE_BUSTER = "cacheBuster";
    private static final String ATTRIBUTE_MEDIA = "media";

    public ClientLibraryHelperFunction() {
        super("clientLibrary");
    }

    /**
     * @param categories
     *
     * @return
     */
    @Override
    public CharSequence execute(final String categories)
            throws Exception {
        StringBuffer buffer = new StringBuffer("");

        boolean jsCategories = false;
        boolean cssCategories = false;
        boolean hasMinificationOption = false;
        boolean forceMinify = false;
        boolean forceBrowserCacheBuster = false;

        TemplateContentModelImpl contentModel = contentModel();
        SlingHttpServletRequest slingRequest = contentModel.request();
        ClientLibraryUtil clientLibraryUtil = new ClientLibraryUtil(htmlLibraryManager, slingRequest);

        // get params
        String clientLibraryType = (String) param(ATTRIBUTE_TYPE);
        String minificationAttribute = (String) param(ATTRIBUTE_MINIFY);
        String browserCacheBusterAttribute = (String) param(ATTRIBUTE_CACHE_BUSTER);
        String mediaAttributeValue = (String) param(ATTRIBUTE_MEDIA);

        if (clientLibraryType != null) {
            if (clientLibraryType.equalsIgnoreCase("js") || clientLibraryType.equalsIgnoreCase("categories")) {
                jsCategories = true;
            }
            if (clientLibraryType.equalsIgnoreCase("css") || clientLibraryType.equalsIgnoreCase("categories")) {
                cssCategories = true;
            }
        }

        if (minificationAttribute != null) {
            try {
                hasMinificationOption = true;
                forceMinify = Boolean.parseBoolean(minificationAttribute);
            } catch (Exception e) {
                log.error("Unable to parse minify option value: ", e);
            }
        }

        if (browserCacheBusterAttribute != null) {
            try {
                forceBrowserCacheBuster = Boolean.parseBoolean(browserCacheBusterAttribute);
            } catch (Exception e) {
                log.error("Unable to parse browser cache buster option value: ", e);
            }
        }

        if (categories != null) {
            log.debug("js: " + jsCategories + " css: " + cssCategories + " forceMinify: " + forceMinify + " cacheBuster: " + forceBrowserCacheBuster + " media: " + mediaAttributeValue);

            clientLibraryUtil.setOptions(jsCategories, cssCategories, hasMinificationOption, forceMinify, forceBrowserCacheBuster, mediaAttributeValue);

            buffer.append(clientLibraryUtil.generateClientLibraries(categories));
        }

        return new Handlebars.SafeString(buffer);
    }

}
