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

package layerx.aem.util;

import com.adobe.granite.ui.clientlibs.ClientLibrary;
import com.adobe.granite.ui.clientlibs.HtmlLibrary;
import com.adobe.granite.ui.clientlibs.HtmlLibraryManager;
import com.adobe.granite.ui.clientlibs.LibraryType;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static layerx.Constants.BLANK;

/**
 * To change this template use File | Settings | File Templates.
 *
 * @author      neozilon
 * @version     1.0.0
 * @since       2013-02-12
 */
public class ClientLibraryUtil {

    private final HtmlLibraryManager htmlLibraryManager;
    private static final String MINIFY_SELECTOR = "min";
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private boolean jsCategories;
    private boolean cssCategories;
    private boolean hasMinificationOption;
    private boolean forceMinify;
    private boolean forceBrowserCacheBuster;
    private String mediaAttributeValue;

    private final SlingHttpServletRequest slingRequest;

    public ClientLibraryUtil(HtmlLibraryManager htmlLibraryManager, SlingHttpServletRequest slingRequest) {
        this.htmlLibraryManager = htmlLibraryManager;
        this.slingRequest = slingRequest;
    }

    /**
     * @param jsCategories This is for flagging whether it's JS or not
     * @param cssCategories This is for flagging whether it's CSS or not
     * @param hasMinificationOption This is for flagging whether it has minify option or not
     * @param forceMinify This is for flagging to minify source
     * @param forceBrowserCacheBuster This is the media attribute value
     */
    public void setOptions(boolean jsCategories, boolean cssCategories, boolean hasMinificationOption, boolean forceMinify, boolean forceBrowserCacheBuster, String mediaAttributeValue) {
        this.jsCategories = jsCategories;
        this.cssCategories = cssCategories;
        this.hasMinificationOption = hasMinificationOption;
        this.forceMinify = forceMinify;
        this.forceBrowserCacheBuster = forceBrowserCacheBuster;
        this.mediaAttributeValue = mediaAttributeValue;
    }

    /**
     * This method generates the clientlib includes for the categories specified, taking into account the options
     * specified using the com.xumak.base.util.ClientLibraryUtil#setOptions(boolean, boolean, boolean, boolean,
     * boolean) method (like minification, cache buster, etc.).
     *
     * @param categories comma separated list with the client libraries categories
     * @return the html tags to include the clientlibs
     */
    public String generateClientLibraries(String categories) {
        StringBuffer buffer = new StringBuffer();
        String[] categoriesArray = splitStringToArray(categories);

        if (this.cssCategories) {
            List<ClientLibrary> cssClientLibraries = new ArrayList<>(htmlLibraryManager.getLibraries(categoriesArray,
                    LibraryType.CSS, false, false));
            buildCssIncludes(cssClientLibraries, buffer);
        }
        if (this.jsCategories) {
            List<ClientLibrary> jsClientLibraries = new ArrayList<>(htmlLibraryManager.getLibraries(categoriesArray,
                    LibraryType.JS, false, false));
            buildJsIncludes(jsClientLibraries, buffer);
        }

        return buffer.toString();
    }

    /**
     * This method generates a pristine clientlib include (no cache buster selector, etc.) calling directly to
     * com.day.cq.widget.HtmlLibraryManager#getLibraries(java.lang.String[], com.day.cq.widget.LibraryType, boolean,
     * boolean)
     *
     * @param categories comma separated list with the client libraries categories
     * @return the html tags to include the clientlibs using pristine urls
     */
    public String generateClientLibrariesPristine(String categories) {
        String result = BLANK;
        StringWriter writer = new StringWriter();
        try {
            if (this.cssCategories && this.jsCategories) {
                htmlLibraryManager.writeIncludes(this.slingRequest, writer, categories);
            } else if (this.cssCategories) {
                htmlLibraryManager.writeCssInclude(this.slingRequest, writer, categories);
            } else if (this.jsCategories) {
                htmlLibraryManager.writeJsInclude(this.slingRequest, writer, categories);
            }
            result = writer.toString();
        } catch (Exception e) {
            log.error("Error generating writer includes...", e);
        }


        return result;
    }

    /**
     * Builds our Javascript include tags given an ArrayList of ClientLibrary objects
     * and a StringBuffer.
     *
     * @param jsLibs -> our list of ClientLibrary objects.
     * @param buffer -> our StringBuffer.
     * @return void
     **/
    private void buildJsIncludes(List<ClientLibrary> jsLibs, StringBuffer buffer) {
        for (ClientLibrary lib : jsLibs) {
            log.debug("JS LIB : " + lib.getPath());
            freshenLibrary(LibraryType.JS, lib.getPath());

            String path = lib.getPath();
            path = this.minificationChecker(path); // check minify option
            path = this.browserCacheOptionChecker(lib.getPath(), path, LibraryType.JS);
            try {
                buffer.append("<script src=\"");
                buffer.append(path + ".js");
                buffer.append("\"></script>\n");
            } catch (Exception e) {
                log.error("Caught exception generating library path", e);
            }

        }
    }

    /**
     * Builds our CSS include tags given an ArrayList of ClientLibrary objects
     * and a StringBuffer.
     *
     * @param cssLibs -> our list of ClientLibrary objects.
     * @param buffer  -> our StringBuffer.
     * @return void
     **/
    protected void buildCssIncludes(List<ClientLibrary> cssLibs, StringBuffer buffer) {
        for (ClientLibrary lib : cssLibs) {
            log.debug("CSS LIB : " + lib.getPath());
            freshenLibrary(LibraryType.CSS, lib.getPath());

            String path = lib.getPath();
            path = this.minificationChecker(path); // check minify option
            path = this.browserCacheOptionChecker(lib.getPath(), path, LibraryType.CSS);
            try {
                buffer.append("<link rel=\"stylesheet\" href=\"");
                buffer.append(path + ".css");
                buffer.append("\" type=\"text/css\"");
                buffer.append(buildMediaAttribute());
                buffer.append(" / >\n");
            } catch (Exception e) {
                log.error("Caught exception generating library path", e);
            }

        }
    }

    /**
     * @param path
     * @return
     */
    private String minificationChecker(String path) {
        StringBuffer newPath = new StringBuffer(path);
        if (forceMinify || (hasMinificationOption && htmlLibraryManager.isMinifyEnabled())) {
            newPath.append(".").append(MINIFY_SELECTOR);
        }
        return newPath.toString();
    }

    /**
     * @return media attribute
     */
    private String buildMediaAttribute() {
        String mediaAttribute = "";
        if (this.mediaAttributeValue != null && !this.mediaAttributeValue.isEmpty()) {
            mediaAttribute = "media=\"" + this.mediaAttributeValue + "\"";
        }
        return mediaAttribute;
    }

    /**
     * @param originalPath
     * @param path
     * @return
     */
    private String browserCacheOptionChecker(String originalPath, String path, LibraryType libType) {
        StringBuffer newPath = new StringBuffer(path);
        if (this.forceBrowserCacheBuster) {

            //generate the lastmodified String
            String suffixBrowserCacheBuster = "";
            try {
                suffixBrowserCacheBuster = this.generateDateTime(originalPath, libType);
            } catch (Exception e) {
                log.error("Error generating DateTime for cache buster. ", e);
            }
            newPath.append(".").append(suffixBrowserCacheBuster);
        }
        return newPath.toString();
    }

    /**
     * This method does not output anything, but is extremely important because the call to
     * HtmlLibrary.getLastModified() checks for the staleness of the library, forcing a recompile
     * if the library is stale.
     *
     * @param type
     * @param path
     * @return void
     */
    protected void freshenLibrary(LibraryType type, String path) {
        HtmlLibrary lib = htmlLibraryManager.getLibrary(type, path);
        if (lib != null) {
            log.debug("HtmlLibrary LastModified : " + lib.getLastModified());
        }
    }

    /**
     * This method is used to generate the date/time selector for a given resource.
     * This method is private, and is only used internal to this service.
     *
     * @param relativePath -> the relative path to the resource from which to generate the selector.
     * @return dateTime -> a generated selector based off of the "jcr:lastModified" property of the asset whose path is passed in.
     * @throws RepositoryException
     * @throws PathNotFoundException
     * @throws ValueFormatException
     */
    protected String generateDateTime(String relativePath, LibraryType libType)
            throws RepositoryException,
            PathNotFoundException,
            ValueFormatException {
        Node resourceNode = null;
        String resourcePath = "/var/clientlibs" + relativePath;
        String extension = (libType.equals(LibraryType.CSS)) ? ".css" : ".js";
        try {
            Session jcrSession = slingRequest.getResourceResolver().adaptTo(Session.class);
            resourceNode = (Node) jcrSession.getItem(resourcePath + extension);
        } catch (Exception e) {
            log.error("Caught exception resolving Node", e);
        }
        // make sure we have a default date/time selector - we should never need this.
        String dateTime = "0";
        // generate date/time selector if we have the jcr:lastModified property
        if (resourceNode != null) {
            if (resourceNode.hasProperty(JcrConstants.JCR_LASTMODIFIED)) {
                dateTime = resourceNode.getProperty(JcrConstants.JCR_LASTMODIFIED).getString();
            } else if (resourceNode.hasNode(JcrConstants.JCR_CONTENT)) {
                Node jcrContentNode = resourceNode.getNode(JcrConstants.JCR_CONTENT);
                if (jcrContentNode.hasProperty(JcrConstants.JCR_LASTMODIFIED)) {
                    dateTime = jcrContentNode.getProperty(JcrConstants.JCR_LASTMODIFIED).getString();
                }
            }
        }
        return dateTime.replace(":", "").replace("+", "").replace("-", "").replace(".", "");
    }

    /**
     * @param commaSeparatedList
     * @return
     */
    private static String[] splitStringToArray(String commaSeparatedList) {
        if (commaSeparatedList == null) {
            return new String[]{};
        }
        String[] split = commaSeparatedList.split(",");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].trim();
        }
        return split;
    }
}
