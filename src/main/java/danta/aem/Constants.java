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

package danta.aem;

import static danta.Constants.SLASH;
import static danta.Constants.PRIVATE_AN_PREFIX;
import static danta.Constants.CLIENT_CONTENT_MODEL_SELECTOR;
import static danta.Constants.PAGE;
import static danta.Constants.COMPONENT;
import static danta.Constants.STATS;

/**
 * Constants for AEM specific.
 *
 * @author      Danta Team
 * @version     1.0.0
 * @since       2017-08-08
 */
public class Constants {
    //general
    public static final String SLING_FOLDER = "sling:Folder";
    public static final String SLING_RESOURCE_TYPE = "sling:resourceType";
    public static final String SLING_RESOURCE_SUPER_TYPE = "sling:resourceSuperType";
    public static final String SLING_VANITY_PATH = "sling:vanityPath";
    public static final String FOUNDATION_IMAGE_COMPONENT_RESOURCE_TYPE = "foundation/components/image";
    public static final String CONTENT_ROOT = "/content";
    public static final String APPS_ROOT = "/apps";
    public static final String JCR_CONTENT = "jcr:content";
    public static final String JCR_DESCRIPTION = "jcr:description";
    public static final String JCR_CREATED = "jcr:created";
    public static final String ETC = "etc";
    public static final String PATH_DETAILS_LIST_PATH_PROPERTY_NAME = "path";
    public static final String PATH_DETAILS_LIST_PATHS_PROPERTY_NAME = "paths";
    //Global properties
    public static final String GLOBAL_DIALOG_PATH = "/components/page/htmlbase/global_dialog";
    public static final String GLOBAL_DIALOG_PATH_TOUCH = "/components/page/htmlbase/cq:global_dialog";
    public static final String GLOBAL_DIALOG_PATH_PROPERTY_KEY = "globalDialogPath";
    public static final String GLOBAL_PATH = "/global";
    public static final String GLOBAL_PATH_PROPERTY_KEY = "globalPath";
    public static final String APP_NAME_PROPERTY_KEY = "appName";
    //Renditions & image transform stuff
    public static final String DEFAULT_RENDITION_NAME = "xk_defaultRenditionName";
    public static final String DEFAULT_SECOND_IMAGE_RENDITION_NAME = "xk_defaultSecondImageRenditionName";
    public static final String AVAILABLE_RENDITIONS = "xk_availableRenditions";
    public static final String IMAGE_RENDITION_PROPERTY_NAME = "imageRenditionName";
    public static final String TRANSFORM_SELECTOR = ".transform";
    //Asset spooling
    public static final String DAM_ROOT = CONTENT_ROOT + "/dam";
    public static final String ASSET_SELECTOR = "asset";
    public static final String ASSET_EXTENSION = "spool";
    public static final String ASSET_DATA = "assetData";
    public static final String ASSET_MIME_TYPE = "assetMimeType";
    public static final String RENDITIONS_NODE = "renditions";
    public static final String DEFAULT_RENDITION = "original";
    public static final String ASSET_CONTENT = JCR_CONTENT + SLASH + RENDITIONS_NODE  + SLASH
            + DEFAULT_RENDITION + SLASH + JCR_CONTENT;
    //Webservice Stuff
    public static final String CLIENT_PAGE_CONTENT_MODEL_REQ_AN = PRIVATE_AN_PREFIX + "clientPageContentModel";
    public static final String CLIENT_COMPONENT_CONTENT_MODEL_REQ_AN = PRIVATE_AN_PREFIX + "clientComponentContentModel";
    public static final String CLIENT_COMPONENT_CONTENT_MODEL_SELECTORS = CLIENT_CONTENT_MODEL_SELECTOR + "." + COMPONENT;
    public static final String CLIENT_PAGE_CONTENT_MODEL_SELECTORS = CLIENT_CONTENT_MODEL_SELECTOR + "." + PAGE;
    public static final String CLIENT_STATISTICS_CONTENT_MODEL_SELECTORS = CLIENT_CONTENT_MODEL_SELECTOR + "." + STATS;

    //Sling
    public static final String SLING_HTTP_REQUEST = PRIVATE_AN_PREFIX + "sling_http_request";
    //Sling models
    public static final String SLING_MODEL_PROPERTIES_KEY = "model";
    public static final String SLING_MODELS_CONFIG_PROPERTY_NAME = "xk_slingModels";
    //Categories
    public static final String SLING_MODELS_CATEGORY = "slingModelData";
    public static final String STRUCTURE_RESOURCES_CATEGORY = "structureResources";


}

