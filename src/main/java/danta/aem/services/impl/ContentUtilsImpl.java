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

package danta.aem.services.impl;

import com.day.cq.contentsync.handler.util.RequestResponseFactory;
import com.day.cq.wcm.api.WCMMode;
import danta.aem.services.ContentUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.engine.SlingRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static danta.Constants.HTTP_GET;

/**
 * The content utils implementer
 *
 * @author      jbarrera
 * @version     1.0.0
 * @since       2016-12-01
 */
@Component
@Service(ContentUtils.class)
public class ContentUtilsImpl
        implements ContentUtils {

    @Reference
    private RequestResponseFactory requestResponseFactory;

    @Reference
    private SlingRequestProcessor requestProcessor;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * This method gets the rendered content in bytes for an AEM resource.
     *
     * @param path of the resource
     * @param request current request
     * @return the rendered output of a resource in bytes
     */
    public byte[] getOutputAsBytes(String path, SlingHttpServletRequest request) {
        return getOutputAsBytes(path, request.getResourceResolver());
    }

    /**
     * This method gets the rendered content in bytes for an AEM resource.
     *
     * @param path of the resource
     * @param resolver current request
     * @return the rendered output of a resource in bytes
     */
    public byte[] getOutputAsBytes(String path, ResourceResolver resolver) {
        byte[] output =  null;

        try {
            HttpServletRequest customRequest = requestResponseFactory.createRequest(HTTP_GET, path);
            WCMMode.DISABLED.toRequest(customRequest);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            HttpServletResponse customResponse = requestResponseFactory.createResponse(byteArrayOutputStream);

            requestProcessor.processRequest(customRequest, customResponse, resolver);
            output = byteArrayOutputStream.toByteArray();

        } catch (ServletException e) {
            log.error(e.toString());
        } catch (IOException e) {
            log.error(e.toString());
        }

        return output;
    }

}