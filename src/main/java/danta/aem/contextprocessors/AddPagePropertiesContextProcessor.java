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

package danta.aem.contextprocessors;

import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.AuthoringUIMode;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.designer.Design;
import com.day.cq.wcm.api.designer.Designer;
import com.google.common.collect.Sets;
import danta.aem.assets.AssetPathService;
import danta.aem.templating.TemplateContentModelImpl;
import danta.aem.util.GeneralRequestObjects;
import danta.aem.util.PageUtils;
import danta.api.ContextProcessor;
import danta.api.ExecutionContext;
import danta.api.configuration.Configuration;
import danta.api.configuration.ConfigurationProvider;
import danta.api.configuration.Mode;
import danta.api.exceptions.ProcessException;
import danta.core.contextprocessors.AbstractCheckComponentCategoryContextProcessor;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Node;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static danta.Constants.*;
import static danta.aem.Constants.JCR_DESCRIPTION;
import static danta.aem.Constants.SLING_HTTP_REQUEST;
import static danta.aem.util.PropertyUtils.propsToMap;
import static danta.core.Constants.XK_CONTAINER_CLASSES_CP;
import static danta.aem.util.PageUtils.getVanityURLs;

/**
 * The context processor for adding page properties to content model
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2014-09-04
 */
@Component(service = ContextProcessor.class)
public class AddPagePropertiesContextProcessor
        extends AbstractCheckComponentCategoryContextProcessor<TemplateContentModelImpl> {

    private static final Set<String> ANY_OF = Collections.unmodifiableSet(Sets.newHashSet(PAGE_CATEGORY));

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
    protected ConfigurationProvider configurationProvider;

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
    protected AssetPathService assetPathService;

    @Override
    public Set<String> anyOf() {
        return ANY_OF;
    }

    @Override
    public int priority() {
        // This processor must be one of the first processors executed.
        return HIGHEST_PRIORITY;
    }

    /**
     * @param executionContext
     * @param contentModel
     * @throws Exception
     */
    @Override
    public void process(final ExecutionContext executionContext, final TemplateContentModelImpl contentModel)
            throws ProcessException {
        try {
            SlingHttpServletRequest request = (SlingHttpServletRequest) executionContext.get(SLING_HTTP_REQUEST);
            Resource resource = request.getResource();
            log.debug("for {}", resource.getPath());
            if (resource != null) {
                ResourceResolver resourceResolver = request.getResourceResolver();
                Designer designer = resourceResolver.adaptTo(Designer.class);
                final PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
                final TagManager tm = (TagManager) resource.getResourceResolver().adaptTo(TagManager.class);
                Page page = pageManager.getContainingPage(resource);
                if (page != null) {
                    if (!contentModel.has(PAGE_PROPERTIES_KEY)) {
                        Configuration configuration = configurationProvider.getFor(page.getContentResource().getResourceType());
                        Collection<String> bodyClasses = configuration.asStrings(XK_CONTAINER_CLASSES_CP, Mode.MERGE);
                        Node pageContentNode = page.getContentResource().adaptTo(Node.class);
                        Map<String, Object> pageContent = propsToMap(pageContentNode.getProperties());
                        pageContent.put(PATH, page.getPath());
                        pageContent.put(PAGE_NAME, page.getName());
                        pageContent.put(LINK, page.getPath() + HTML_EXT);
                        pageContent.put(BODY_CLASSES, bodyClasses);
                        pageContent.put(TITLE, page.getTitle());
                        pageContent.put(DESCRIPTION, page.getProperties().get(JCR_DESCRIPTION, ""));
                        pageContent.put(PAGE_TITLE, page.getProperties().get(PAGE_TITLE, ""));
                        pageContent.put(SUBTITLE, page.getProperties().get(SUBTITLE, ""));
                        pageContent.put(HIDE_IN_NAV, page.getProperties().get(HIDE_IN_NAV, ""));
                        pageContent.put(KEYWORDS, PageUtils.getKeywords(pageContent, tm));
                        pageContent.put(TAGS, PageUtils.getTags(pageContent));
                        pageContent.put(WCM_MODE, GeneralRequestObjects.getWCMModeString(request));
                        pageContent.put(IS_EDIT_MODE, GeneralRequestObjects.isEditMode(request));
                        pageContent.put(IS_DESIGN_MODE, GeneralRequestObjects.isDesignMode(request));
                        pageContent.put(IS_EDIT_OR_DESIGN_MODE, GeneralRequestObjects.isEditOrDesignMode(request));

                        if (designer != null) {
                            Design design = designer.getDesign(page);
                            if (design != null && design.getPath() != null) {
                                pageContent.put(FAVICON, design.getPath() + "/" + FAVICON + ICO_EXT);
                            }
                        }

                        String navigationTitle = PageUtils.getNavigationTitle(page);
                        if (null != navigationTitle) {
                            pageContent.put(NAVIGATION_TITLE, PageUtils.getNavigationTitle(page));
                        }
                        // add transformed path image
                        String pageImagePath = assetPathService.getPageImagePath(page, page.getContentResource());
                        if(StringUtils.isNotEmpty(pageImagePath)){
                            pageContent.put(IMAGE_PATH, pageImagePath);
                        }

                        // add interface mode
                        if (AuthoringUIMode.fromRequest(request) == AuthoringUIMode.TOUCH) {
                            pageContent.put(IS_TOUCH_UI_MODE, true);
                            pageContent.put(IS_CLASSIC_UI_MODE, false);
                        } else {
                            pageContent.put(IS_CLASSIC_UI_MODE, true);
                            pageContent.put(IS_TOUCH_UI_MODE, false);
                        }

                        // Adding vanity path
                        Object vanityURLs = getVanityURLs(page);
                        if (vanityURLs != null) {
                            pageContent.put(VANITY_PATH, vanityURLs);
                        }

                        contentModel.set(PAGE_PROPERTIES_KEY, pageContent);
                    }
                }
            }
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }
}
