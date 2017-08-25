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

package danta.aem.contextprocessors.lists;

import com.day.cq.wcm.api.Page;
import danta.aem.assets.AssetPathService;
import danta.aem.util.PageUtils;
import danta.aem.util.ResourceUtils;
import danta.api.TemplateContentModel;
import danta.api.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static danta.Constants.*;
import static danta.aem.Constants.JCR_CREATED;
import static danta.aem.Constants.JCR_DESCRIPTION;

/**
 * The abstraction context processor for building a map of property object from a page detail
 *
 * @author      jbarrera
 * @version     1.0.0
 * @since       2014-08-16
 */
@Component(componentAbstract = true)
@Service
public abstract class AbstractPageDetailsContextProcessor extends
        AbstractItemListContextProcessor<TemplateContentModel> {

    @Reference
    protected AssetPathService assetPathService;

    protected Map<String, Object> extractBasicPageDetails(Page page, Resource componentResource, String currentPage) throws Exception{
        Map<String, Object> pageDetails = new HashMap<>();
        if(null != page) {
            pageDetails.put(TITLE, page.getTitle());
            pageDetails.put(NAME, page.getName());
            pageDetails.put(PATH, page.getPath());
            pageDetails.put(LINK, page.getPath() + HTML_EXT);
            pageDetails.put(DESCRIPTION, page.getProperties().get(JCR_DESCRIPTION, ""));
            pageDetails.put(SUBTITLE, page.getProperties().get(SUBTITLE, ""));
            pageDetails.put(CREATED, page.getProperties().get(JCR_CREATED,""));
            pageDetails.put(PAGE_TITLE, page.getProperties().get(PAGE_TITLE, ""));
            String navigationTitle = PageUtils.getNavigationTitle(page);

            if(null != navigationTitle) {
                pageDetails.put(NAVIGATION_TITLE, navigationTitle);
            }
            String pageImagePath = assetPathService.getPageImagePath(page, componentResource);
            if (StringUtils.isNotEmpty(pageImagePath)) {
                pageDetails.put(IMAGE_PATH, pageImagePath);
            }
            String vanityPath = page.getVanityUrl();
            pageDetails.put(VANITY_PATH, null != vanityPath? vanityPath : page.getPath());
            if (currentPage.equals(page.getPath())) {
                pageDetails.put(IS_CURRENT_PAGE, true);
            }
        }

        //Extra properties
        Configuration configuration = configurationProvider.getFor(componentResource.getResourceType());
        Collection<String> extraPropertyNames = configuration.asStrings(EXTRA_LIST_PROPERTIES_CONFIG_KEY);
        for (String extraPropertyName : extraPropertyNames) {
            pageDetails.put(extraPropertyName, PageUtils.getPageProperty(page, extraPropertyName));
        }

        return pageDetails;
    }

}
