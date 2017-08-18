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

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.api.designer.Designer;
import com.day.cq.wcm.api.designer.Style;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

/**
 * Container for the CQ general objects.
 *
 * @author      Atilio Garcia
 * @version     1.0.0
 * @since       2013-11-18
 */
public class GeneralRequestObjects {

    /**
     * Get current resource via SlingHttpServletRequest.
     *
     * @param request This is a SlingHttpServletRequest
     * @return resource This is the current resource
     */
    public static Resource getCurrentResource(final SlingHttpServletRequest request) {
        return request.getResource();
    }

    /**
     * Get resource resolver via SlingHttpServletRequest.
     *
     * @param request This is a SlingHttpServletRequest
     * @return resourceResolver This is an instance of ResourceResolver
     */
    public static ResourceResolver getResourceResolver(final SlingHttpServletRequest request) {
        return request.getResourceResolver();
    }

    /**
     * Get Page Manager via SlingHttpServletRequest.
     *
     * @param request This is a SlingHttpServletRequest
     * @return pageManager This is an instance of PageManager
     */
    public static PageManager getPageManager(final SlingHttpServletRequest request) {
        return getResourceResolver(request).adaptTo(PageManager.class);
    }

    /**
     * Get current page via SlingHttpServletRequest.
     *
     * @param request This is a SlingHttpServletRequest
     * @return page This is the current page
     */
    public static Page getCurrentPage(final SlingHttpServletRequest request) {
        return getPageManager(request).getContainingPage(getCurrentResource(request));
    }

    /**
     * Check if it's in edit mode via SlingHttpServletRequest.
     *
     * @param request This is a SlingHttpServletRequest
     * @return true or false
     */
    public static boolean isEditMode(final SlingHttpServletRequest request) {
        return "EDIT".equals(getWCMModeString(request));
    }

    /**
     * Check if it is in design mode via SlingHttpServletRequest.
     *
     * @param request This is a SlingHttpServletRequest
     * @return true or false
     */
    public static boolean isDesignMode(final SlingHttpServletRequest request) {
        return "DESIGN".equals(getWCMModeString(request));
    }

    /**
     * Check if it's either edit or design mode via SlingHttpServletRequest.
     *
     * @param request This is a SlingHttpServletRequest
     * @return true or false
     */
    public static boolean isEditOrDesignMode(final SlingHttpServletRequest request){
        return isEditMode(request) || isDesignMode(request);
    }

    /**
     * Get WCM Mode as string via SlingHttpServletRequest.
     *
     * @param request This is a SlingHttpServletRequest
     * @return wcmMode This is a string of WCM Mode
     */
    public static String getWCMModeString(final SlingHttpServletRequest request) {
        return WCMMode.fromRequest(request).toString();
    }

    /**
     * Get current style via SlingHttpServletRequest.
     *
     * @param request This is a SlingHttpServletRequest
     * @return style This is a current design style path
     */
    public static Style getCurrentStyle(final SlingHttpServletRequest request) {
        Designer designer = getResourceResolver(request).adaptTo(Designer.class);
        return designer.getStyle(getCurrentResource(request));
    }

}
