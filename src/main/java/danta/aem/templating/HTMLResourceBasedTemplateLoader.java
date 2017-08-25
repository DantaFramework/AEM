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

import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.commons.WCMUtils;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.jknack.handlebars.io.TemplateSource;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.*;
import java.util.Calendar;

import static danta.Constants.BLANK;
import static danta.Constants.HTML_EXT;

/**
 * HTML Resource Based Template loader (Template Loader implementer)
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2013-11-09
 */
public class HTMLResourceBasedTemplateLoader
        implements TemplateLoader {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    protected Resource resource;
    protected Component component;
    protected String prefix = "";
    protected String suffix = "";

    public HTMLResourceBasedTemplateLoader(Resource resource)
            throws Exception {
        this.resource = resource;
        this.component = WCMUtils.getComponent(resource);
    }

    @Override
    public TemplateSource sourceAt(final String location)
            throws IOException {
        Resource scriptResource;
        if (location.startsWith("/")) {
            ResourceResolver resourceResolver = resource.getResourceResolver();
            scriptResource = resourceResolver.getResource(ResourceUtil.normalize(location));
        } else {
            scriptResource = component.getLocalResource(resolve(location));
        }
        if (scriptResource == null || ResourceUtil.isNonExistingResource(scriptResource))
            throw new IOException("Unable to resolve " + location + " to a valid Resource path.");
        else
            return new HTMLFileTemplateSource(scriptResource);
    }

    @Override
    public String resolve(final String location) {
        String fullLocation = (location.startsWith("/")) ? ResourceUtil.normalize(location) : new StringBuilder(location).append(HTML_EXT).toString();
        return fullLocation;
    }

    @Override
    public String getPrefix() {
        return BLANK;
    }

    @Override
    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getSuffix() {
        return BLANK;
    }

    @Override
    public void setSuffix(final String suffix) {
        this.suffix = suffix;
    }

    public static class HTMLFileTemplateSource
            implements TemplateSource {

        private final Resource resource;

        public HTMLFileTemplateSource(final Resource scriptResource)
                throws FileNotFoundException {
            if (scriptResource == null || ResourceUtil.isNonExistingResource(scriptResource))
                throw new FileNotFoundException("No file with the path " + scriptResource.getName() + " was found.");
            this.resource = scriptResource;
        }

        @Override
        public String content()
                throws IOException {
            return IOUtils.toString(inputStream(), "UTF-8");
        }

        protected InputStream inputStream()
                throws IOException {
            return resource.adaptTo(InputStream.class);
        }

        @Override
        public String filename() {
            return resource.getPath();
        }

        @Override
        public long lastModified() {
            try {
                return JcrUtils.getLastModified(resource.adaptTo(Node.class)).getTimeInMillis();
            } catch (RepositoryException ew) {
                return Calendar.getInstance().getTimeInMillis();
            }
        }
    }
}
