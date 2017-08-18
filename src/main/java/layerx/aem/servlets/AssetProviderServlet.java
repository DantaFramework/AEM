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

package layerx.aem.servlets;

import com.adobe.acs.commons.images.NamedImageTransformer;
import com.adobe.acs.commons.util.PathInfoUtil;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.DamUtil;
import com.day.image.Layer;
import com.google.common.io.ByteStreams;
import layerx.aem.util.ImageUtils;
import layerx.aem.util.ResourceUtils;
import layerx.api.configuration.ConfigurationProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Component;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.OptingServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.day.cq.commons.jcr.JcrConstants.JCR_DATA;
import static com.day.cq.commons.jcr.JcrConstants.JCR_MIMETYPE;
import static layerx.Constants.*;
import static layerx.aem.Constants.*;

/**
 * The Asset Provider Servlet provides access to DAM assets in AEM without exposing the JCR structure.
 * This Servlet is used by the Image components:
 * <p>
 * Url example:
 * <pre><blockquote>
 *     http://localhost:4502/content/test/_jcr_content/examplePar/singleimage_0.asset.spool/resources.jpg
 * </blockquote></pre>
 *
 * @author      Pablo Alecio
 * @version     1.0.0
 * @since       2013-11-15
 */
@Component(
        label = "Asset Provider Servlet",
        description = "Uses the path to the resource to retrieve the DAM Asset related to it.",
        metatype = true
)
@Properties({
        @Property(
                label = "Resource Types",
                description = "Resource Types and Node Types to bind this servlet to.",
                name = "sling.servlet.resourceTypes",
                value = {"sling/servlet/default", FOUNDATION_IMAGE_COMPONENT_RESOURCE_TYPE},
                propertyPrivate = false
        ),
        @Property(
                label = "Max dimensions",
                description = "All images bigger than these dimensions will be resized before applying the transforms" +
                        "Note: if the image path doesn't contain any transform, the image won't be resized. " +
                        "Format: widthxheight",
                name = AssetProviderServlet.MAX_DIMENSIONS_PN,
                value = {"1280x720"},
                propertyPrivate = false
        ),
        @Property(
                label = "Extension",
                description = "",
                name = "sling.servlet.extensions",
                value = { ASSET_EXTENSION },
                propertyPrivate = true
        ),
        @Property(
                name = "sling.servlet.methods",
                value = { "GET" },
                propertyPrivate = true
        ),
        @Property(name = "sling.servlet.selectors",
                value = ASSET_SELECTOR,
                propertyPrivate = true)
})
@Reference(
        name = "namedImageTransformers",
        referenceInterface = NamedImageTransformer.class,
        policy = ReferencePolicy.DYNAMIC,
        cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE
)
@Service(Servlet.class) //TODO: Create documentation and add javadocs
@SuppressWarnings("unchecked")
public class AssetProviderServlet extends SlingSafeMethodsServlet implements OptingServlet{

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY, policy = ReferencePolicy.STATIC)
    protected ResourceResolverFactory resourceResolverFactory;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY, policy = ReferencePolicy.STATIC)
    protected ConfigurationProvider configurationProvider;

    private Map<String, NamedImageTransformer> namedImageTransformers = new HashMap();
    private final Logger LOGGER = LoggerFactory.getLogger(AssetProviderServlet.class);
    protected static final String MAX_DIMENSIONS_PN = "maxDimensions";
    private int maxHeight = 1280;
    private int maxWidth = 720;


    /**
     * Only accept requests that.
     * - Are not null
     * - Have a suffix
     * - if the suffix has 3 segments, the first suffix segment is a registered transform name
     *
     * @param request SlingRequest object
     * @return true if the Servlet should handle the request
     */
    @Override
    public final boolean accepts(SlingHttpServletRequest request) {
        if (request == null) {
            return false;
        }

        final String suffix = request.getRequestPathInfo().getSuffix();
        if (StringUtils.isBlank(suffix)) {
            return false;
        }

        String[] selectors = request.getRequestPathInfo().getSelectors();
        if (selectors.length == 2) {
            int imageNumber = NumberUtils.toInt(selectors[1], -1);
            if (imageNumber < 0) {
                return false;
            }
        }

        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws IOException, ServletException {
        //TODO: put all of the logic in a context processor (need to fix templating support filter bug first)
        String transformName = BLANK;
        if (PathInfoUtil.getSuffixSegments(request).length == 2) {
            String firstSuffixSegment = PathInfoUtil.getFirstSuffixSegment(request);
            if (this.namedImageTransformers.keySet().contains(firstSuffixSegment)) {
                transformName = firstSuffixSegment;
            }
        }
        //Adds the asset binary to the inputStream
        try {
            Resource assetResource = getAssetResource(request);

            if (DamUtil.isAsset(assetResource)) {
                Binary binary;
                String mimeType = BLANK;
                Asset asset = DamUtil.resolveToAsset(assetResource);
                Resource original = asset.getOriginal();

                Node assetNode = original.adaptTo(Node.class);
                if (assetNode.hasNode(JCR_CONTENT)) {
                    Node assetInfo = assetNode.getNode(JCR_CONTENT);
                    if (assetInfo.hasProperty(JCR_MIMETYPE)) {
                        mimeType = assetInfo.getProperty(JCR_MIMETYPE).getString();
                    }
                    if (StringUtils.isNotBlank(mimeType)) {
                        response.setContentType(mimeType);
                    }
                    binary = assetInfo.getProperty(JCR_DATA).getBinary();
                    InputStream inputStream = binary.getStream();
                    OutputStream outputStream = response.getOutputStream();

                    boolean shouldTransform = StringUtils.isNotBlank(transformName);
                    if (shouldTransform && ImageUtils.isImage(assetResource)) {
                        double quality = 1;
                        double maxGifQuality = 255;

                        // Transform the image
                        final Layer layer = new Layer(inputStream, new Dimension(maxWidth, maxHeight));
                        Layer newLayer = null;
                        try {
                            final NamedImageTransformer namedImageTransformer = this.namedImageTransformers.get(transformName);
                            newLayer = namedImageTransformer.transform(layer);

                            if (StringUtils.isBlank(mimeType) || !ImageIO.getImageWritersByMIMEType(mimeType).hasNext()) {
                                mimeType = getImageMimeType(layer, asset.getName());
                                response.setContentType(mimeType);
                            }
                            // For GIF images the colors will be reduced according to the quality argument.
                            if (StringUtils.equals(mimeType, GIF_MIME_TYPE)) {
                                quality = quality * maxGifQuality;
                            }

                            newLayer.write(mimeType, quality, outputStream);
                        } finally {
                            if (layer != null) {
                                layer.dispose();
                            }
                            if (newLayer != null) {
                                newLayer.dispose();
                            }
                        }

                    } else {
                        ByteStreams.copy(inputStream, outputStream);
                    }

                    response.flushBuffer();
                    outputStream.close();

                }
            }
        } catch (RepositoryException repoException) {
            LOGGER.error("Repository Exception. ", repoException);
        }
    }

    protected final void bindNamedImageTransformers(final NamedImageTransformer service,
                                                    final Map<Object, Object> props) {
        final String type = PropertiesUtil.toString(props.get(NamedImageTransformer.PROP_NAME), null);
        if (type != null) {
            this.namedImageTransformers.put(type, service);
        }
    }

    protected final void unbindNamedImageTransformers(final NamedImageTransformer service,
                                                      final Map<Object, Object> props) {
        final String type = PropertiesUtil.toString(props.get(NamedImageTransformer.PROP_NAME), null);
        if (type != null) {
            this.namedImageTransformers.remove(type);
        }
    }

    private Resource getAssetResource(SlingHttpServletRequest request) {
        Resource componentResource = request.getResource();
        String damAssetPath = BLANK;
        String[] selectors = request.getRequestPathInfo().getSelectors();

        int assetIndex = -1;
        if(selectors.length == 2) {
            assetIndex = Integer.parseInt(selectors[1]);
        }

        if (assetIndex == -1) {
            damAssetPath = ResourceUtils.getPropertyAsString(componentResource, FILE_REFERENCE);
        } else {
            List<String> fileReferences = ResourceUtils.getPropertyAsStrings(componentResource, FILE_REFERENCES);
            if(fileReferences.size() > assetIndex) {
                damAssetPath = fileReferences.get(assetIndex);
            }
        }
        return request.getResourceResolver().getResource(damAssetPath);
    }

    @Activate
    protected void activate(final ComponentContext context) {
        String maxDimensionsValue = getProperty(MAX_DIMENSIONS_PN, context);
        String[] maxDimensionsArray = maxDimensionsValue.split("x");
        if (maxDimensionsArray.length == 2) {
            maxHeight = NumberUtils.toInt(maxDimensionsArray[0], maxHeight);
            maxWidth = NumberUtils.toInt(maxDimensionsArray[1], maxWidth);
        }
    }

    private String getProperty(String propertyName, ComponentContext context) {
        final Dictionary properties = context.getProperties();
        return PropertiesUtil.toString(properties.get(propertyName), BLANK);
    }

    /**
     * if the extension is .png, it returns "image/png" else it returns "image/jpeg"
     * TODO: add support for other image types.
     * @param filename e.g image.jpg
     * @return the mime type
     */
    private String getImageMimeType(Layer layer, String filename) {
        String mimeType = layer.getMimeType();
        if (mimeType == null) {
            String[] filenameArray = filename.split("\\.");
            if (filenameArray.length > 0 && filenameArray[filenameArray.length - 1].equals(PNG)) {
                mimeType = PNG_MIME_TYPE;
            } else {
                mimeType = JPG_MIME_TYPE;
            }
        }
        return mimeType;
    }

}
