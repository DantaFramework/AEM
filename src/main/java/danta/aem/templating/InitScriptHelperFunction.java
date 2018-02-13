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

import com.adobe.granite.ui.clientlibs.HtmlLibraryManager;
import com.day.cq.wcm.api.AuthoringUIMode;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.components.EditContext;
import com.day.cq.wcm.commons.WCMUtils;
import com.day.cq.wcm.undo.UndoConfigService;
import com.github.jknack.handlebars.Handlebars;
import danta.aem.util.ClientLibraryUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import java.io.StringWriter;

import static danta.Constants.BLANK;

/**
 * Init Script Helper Function
 *
 * @author      neozilon
 * @version     1.0.0
 * @since       2013-02-12
 */
@Component(service = HelperFunction.class)
public class InitScriptHelperFunction
        extends AbstractAEMHelperFunction<Object> {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
    HtmlLibraryManager htmlLibraryManager;

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
    UndoConfigService undoConfigService;

    public InitScriptHelperFunction() {
        super("initWCMScript");
    }

    @Override
    public CharSequence execute(final Object valueObj)
            throws Exception {

        StringBuffer buffer = new StringBuffer();
        TemplateContentModelImpl contentModel = contentModel();
        SlingHttpServletRequest slingRequest = contentModel.request();

        WCMMode wcmMode = WCMMode.fromRequest(slingRequest);
        ClientLibraryUtil clientLibUtil = new ClientLibraryUtil(htmlLibraryManager, slingRequest);
        clientLibUtil.setOptions(true, true, false, false, false, BLANK);

        if (wcmMode != WCMMode.DISABLED) {
            ComponentContext componentContext = WCMUtils.getComponentContext(slingRequest);
            EditContext editContext = componentContext.getEditContext();
            String dlgPath = null;
            if (editContext != null && editContext.getComponent() != null) {
                dlgPath = editContext.getComponent().getDialogPath();
            }

            if (AuthoringUIMode.fromRequest(slingRequest) == AuthoringUIMode.TOUCH) {
                buffer.append(clientLibUtil.generateClientLibrariesPristine("cq.authoring.page"));
            } else
            if (AuthoringUIMode.fromRequest(slingRequest) == AuthoringUIMode.CLASSIC) {
                buffer.append(clientLibUtil.generateClientLibrariesPristine("cq.wcm.edit"));
                boolean isEditMode = (wcmMode == WCMMode.EDIT) ? true : false;
                String dialogPath = dlgPath == null ? "" : dlgPath;
                buffer.append(this.getJavaScript(slingRequest, isEditMode, dialogPath));
            }
        }

        return new Handlebars.SafeString(buffer);
    }

    private String getJavaScript(SlingHttpServletRequest slingRequest, boolean isEditMode, String dialogPath) {
        StringBuffer buffer = new StringBuffer();


        buffer.append("<script type=\"text/javascript\"> \n" +
                " (function() {\n" +
                "var cfg = ");
        try {
            StringWriter writer = new StringWriter();
            undoConfigService.writeClientConfig(writer);
            buffer.append(writer.getBuffer());
        } catch (Exception e) {
            // ignore
        }

        Node currentNode = slingRequest.getResource().adaptTo(Node.class);
        Page currentPage = null;
        try {
            if (currentNode.getName().equals("jcr:content")) {
                ResourceResolver resourceResolver = slingRequest.getResourceResolver();
                currentPage = resourceResolver.resolve(currentNode.getParent().getPath()).adaptTo(Page.class);
            }
        } catch (Exception e) {
            LOG.error("Error trying to access the currentPage: " + e);
        }

        if (currentPage != null) {
            buffer.append("; \n cfg.pagePath = \"").append(currentPage.getPath()).append("\"; \n");
            buffer.append(" if (CQClientLibraryManager.channelCB() != \"touch\") { \n" +
                    "    var isEditMode = " + isEditMode + "; \n");
            buffer.append("if (!isEditMode) {\n" +
                    "           cfg.enabled = false;\n" +
                    "      }\n" +
                    "      CQ.undo.UndoManager.initialize(cfg);\n" +
                    "      CQ.Ext.onReady(function() {" +
                    "          CQ.undo.UndoManager.detectCachedPage(" + System.currentTimeMillis() + ");\n" +
                    "      });" +
                    "   }\n" +
                    "   })();" +
                    "   CQ.WCM.launchSidekick(\"" + currentPage.getPath() + "\", {\n" +
                    "                    propsDialog: \"" + dialogPath + "\",\n" +
                    "                    locked: " + currentPage.isLocked() + "\n" +
                    "                });\n" +
                    "            </script>");
        }

        return buffer.toString();

    }

}
