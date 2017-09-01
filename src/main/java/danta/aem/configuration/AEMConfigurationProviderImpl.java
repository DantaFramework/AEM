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

package danta.aem.configuration;

import com.day.cq.wcm.api.components.ComponentManager;
import com.google.common.collect.Lists;
import danta.aem.util.PropertyUtils;
import danta.aem.util.ResourceUtils;
import danta.api.configuration.Configuration;
import danta.api.configuration.ConfigurationProvider;
import danta.api.configuration.Mode;
import danta.core.util.OSGiUtils;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.resource.JcrResourceUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.Property;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;
import java.util.*;

import static danta.Constants.*;
import static danta.aem.Constants.APPS_ROOT;

/**
 * Configuration (xk config) provider implementation for AEM
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2014-03-17
 */
@Component(immediate = true)
@Service({EventListener.class, ConfigurationProvider.class})
public class AEMConfigurationProviderImpl
        implements ConfigurationProvider<String>, EventListener {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY, policy = ReferencePolicy.STATIC)
    private ResourceResolverFactory resourceResolverFactory;

    private static final Mode DEFAULT_MODE = Mode.INHERIT; // TODO: Make configurable

    private static final String CONFIG_SERVICE = "config-service";
    private static final Map<String, Object> RESOURCE_RESOLVER_PARAMS =
            Collections.unmodifiableMap(Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, CONFIG_SERVICE));


    /**
     * Check if the given resource type has configuration
     *
     * @param resourceType The resource type to check if it has any configuration node
     * @return true if exists; else false
     * @throws Exception
     */
    @Override
    public boolean hasConfig(String resourceType)
            throws Exception {
        boolean hasConfigNode = false;
        ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(RESOURCE_RESOLVER_PARAMS);

        ComponentManager componentManager = resourceResolver.adaptTo(ComponentManager.class);
        com.day.cq.wcm.api.components.Component component = componentManager.getComponent(resourceType);

        if (component != null) {
            Resource configResource = component.getLocalResource(XK_CONFIG_RESOURCE_NAME);
            if (configResource != null) {
                hasConfigNode = true;
            }
        }
        resourceResolver.close();

        return hasConfigNode;
    }

    /**
     * Get configuration for the given resource type
     *
     * @param resourceType The resource type to fetch the configuration from
     * @return configuration The configuration object
     * @throws Exception
     */
    @Override
    public Configuration getFor(String resourceType)
            throws Exception {
        return new ConfigurationImpl(resourceType);
    }

    @Reference
    private SlingRepository repository;

    private final Map<String, Map<String, Map<String, InertProperty>>> configCache = new HashMap<>();

    /**
     * Inner class: Configuration implementer
     */
    private class ConfigurationImpl
            implements Configuration {

        //private Resource componentResource;
        private com.day.cq.wcm.api.components.Component component;
        private ComponentManager componentManager;
        private ResourceResolver resourceResolver;

        private ConfigurationImpl(String resourceType)
                throws Exception {
            resourceResolver = resourceResolverFactory.getServiceResourceResolver(RESOURCE_RESOLVER_PARAMS);

            componentManager = resourceResolver.adaptTo(ComponentManager.class);
            component = componentManager.getComponent(resourceType);
            loadConfigHierarchy();
            resourceResolver.close();
        }

        @Override
        public Mode defaultMode() {
            return DEFAULT_MODE;
        }

        private Map<String, Map<String, InertProperty>> configMembers = new LinkedHashMap<>();
        private Set<String> propNamesDeepCache = Collections.emptySet();

        private Map<String, InertProperty>
        getNodePropertiesMap(Node node, Map<String, InertProperty> propsMap, String propertyPrefix)
                throws Exception {
            NodeIterator nodeIterator = node.getNodes();
            while (nodeIterator.hasNext()) {
                Node childNode = nodeIterator.nextNode();
                String childNodePropertyPrefix = propertyPrefix + childNode.getName() + DOT;
                getNodePropertiesMap(childNode, propsMap, childNodePropertyPrefix);
            }

            //add properties to map
            PropertyIterator props = node.getProperties();
            while (props.hasNext()) {
                InertProperty property = new InertProperty(props.nextProperty());
                if (!StringUtils.startsWithAny(property.name(), RESERVED_SYSTEM_NAME_PREFIXES)) {
                    propsMap.put(propertyPrefix + property.name(), property);
                }
            }
            return propsMap;
        }

        private void loadConfigHierarchy()
                throws Exception {
            if (component != null) {
                if (!configCache.containsKey(component.getResourceType())) {
                    com.day.cq.wcm.api.components.Component aComponent = component;
                    while (aComponent != null) {
                        Resource configResource = aComponent.getLocalResource(XK_CONFIG_RESOURCE_NAME);
                        if (configResource == null)
                            break;
                        Resource foundComponentResource = configResource.getParent();
                        if (!foundComponentResource.getPath().equals(aComponent.getPath())) {
                            aComponent = componentManager.getComponent(foundComponentResource.getPath());
                        }
                        Node node = configResource.adaptTo(Node.class);

                        Map<String, InertProperty> propsMap = getNodePropertiesMap(node, new HashMap<String, InertProperty>(), BLANK);
                        configMembers.put(aComponent.getResourceType(), propsMap);

                        aComponent = aComponent.getSuperComponent();
                    }
                    configCache.put(component.getResourceType(), configMembers);
                } else {
                    configMembers = configCache.get(component.getResourceType());
                }
            }
            propNamesDeepCache = names(false);

        }

        private Set<String> names(boolean shallow) {
            Set<String> names = new HashSet<>();
            for (String configMemberPath : configMembers.keySet()) {
                for (String propName : configMembers.get(configMemberPath).keySet()) {
                    names.add(propName);
                }
                if (shallow)
                    break;
            }
            return names;
        }


        private Collection<Value> valuesFor(String paramName, Mode mode)
                throws Exception {

            Collection<Value> values = Collections.emptyList();

            switch (mode) {
                case INHERIT:
                    values = getInherited(paramName);
                    break;
                case MERGE:
                    values = getMerged(paramName);
                    break;
                case COMBINE:
                    values = getCombined(paramName);
                    break;
                case SHALLOW:
                    values = getShallow(paramName);
                    break;
                default:
                    break;
            }
            return values;
        }

        private Collection<Value> getInherited(String paramName)
                throws Exception {
            Collection<Value> values = Collections.emptyList();
            for (String memberComp : configMembers.keySet()) {
                Map<String, InertProperty> props = configMembers.get(memberComp);
                if (props.containsKey(paramName)) {
                    values = toCollection(props.get(paramName));
                    break;
                }
            }
            return values;
        }

        private Collection<Value> getMerged(String paramName)
                throws Exception {
            Collection<Value> values = new ArrayList<>();
            for (String memberComp : configMembers.keySet()) {
                Map<String, InertProperty> props = configMembers.get(memberComp);
                if (props.containsKey(paramName)) {
                    for (Value value : toCollection(props.get(paramName))) {
                        if (!values.contains(value)) {
                            values.add(value);
                        }
                    }
                }
            }
            return values;
        }

        private Collection<Value> getCombined(String paramName)
                throws Exception {
            Collection<Value> values = new ArrayList<>();
            for (String memberComp : configMembers.keySet()) {
                Map<String, InertProperty> props = configMembers.get(memberComp);
                if (props.containsKey(paramName)) {
                    values.addAll(toCollection(props.get(paramName)));
                }
            }
            return values;
        }

        private Collection<Value> getShallow(String paramName)
                throws Exception {
            Collection<Value> values = Collections.emptyList();
            for (String memberComp : configMembers.keySet()) {
                Map<String, InertProperty> props = configMembers.get(memberComp);
                if (props.containsKey(paramName)) {
                    values = toCollection(props.get(paramName));
                }
                break;
            }
            return values;
        }

        private Collection<Value> toCollection(InertProperty property)
                throws Exception {
            return property.values();
        }

        @Override
        public Set<String> names(Mode mode)
                throws Exception {
            return (mode == Mode.SHALLOW) ? names(true) : propNamesDeepCache;
        }

        @Override
        public Set<String> names()
                throws Exception {
            return names(defaultMode());
        }

        public Map<String, Object> distilledMap(Mode mode, boolean flatten)
                throws Exception {
            Map<String, Object> distilledMap = new HashMap<>();
            for (String paramName : names(mode)) {
                List<Object> objs = new ArrayList<>();
                for (Value value : valuesFor(paramName, mode)) {
                    objs.add(JcrResourceUtil.toJavaObject(value));
                }
                Object distilledValue;
                switch (objs.size()) {
                    case 0:
                        distilledValue = (flatten) ? null : Collections.emptySet();
                        break;
                    case 1:
                        if (flatten) {
                            distilledValue = objs.get(0);
                            break;
                        }
                    default:
                        distilledValue = objs;
                }
                distilledMap.put(paramName, distilledValue);
            }
            return distilledMap;
        }

        public Map<String, Object> distilledMap(Mode mode)
                throws Exception {
            return distilledMap(mode, true);
        }

        public Map<String, Object> distilledMap()
                throws Exception {
            return distilledMap(defaultMode());
        }

        @Override
        public String asString(String paramName, Mode mode)
                throws Exception {
            List<String> strings = asStrings(paramName, mode);
            return strings.isEmpty() ? BLANK : strings.get(0);
        }

        @Override
        public List<String> asStrings(String paramName, Mode mode)
                throws Exception {
            return PropertyUtils.as(String.class, valuesFor(paramName, mode));
        }

        @Override
        public String asString(String paramName)
                throws Exception {
            return asString(paramName, defaultMode());
        }

        @Override
        public List<String> asStrings(String paramName)
                throws Exception {
            return asStrings(paramName, defaultMode());
        }

        @Override
        public Number asNumber(String paramName, Mode mode)
                throws Exception {
            List<Number> numbers = asNumbers(paramName, mode);
            return numbers.isEmpty() ? 0 : numbers.get(0);
        }

        @Override
        public List<Number> asNumbers(String paramName, Mode mode)
                throws Exception {
            return PropertyUtils.as(Number.class, valuesFor(paramName, mode));
        }

        @Override
        public Number asNumber(String paramName)
                throws Exception {
            return asNumber(paramName, defaultMode());
        }

        @Override
        public Collection<Number> asNumbers(String paramName)
                throws Exception {
            return asNumbers(paramName, defaultMode());
        }

        @Override
        public Date asDate(String paramName, Mode mode)
                throws Exception {
            List<Date> dates = asDates(paramName, mode);
            return dates.isEmpty() ? Calendar.getInstance().getTime() : dates.get(0);
        }

        @Override
        public List<Date> asDates(String paramName, Mode mode)
                throws Exception {
            return PropertyUtils.as(Date.class, valuesFor(paramName, mode));
        }

        @Override
        public Date asDate(String paramName)
                throws Exception {
            return asDate(paramName, defaultMode());
        }

        @Override
        public Collection<Date> asDates(String paramName)
                throws Exception {
            return asDates(paramName, defaultMode());
        }

        @Override
        public String toString() {
            try {
                return new JSONObject(distilledMap()).toJSONString();
            } catch (Exception ew) {
                log.error(ERROR, ew);
            }
            return null;
        }

        @Override
        public Map<String, Object> toMap()
                throws Exception {
            return distilledMap();
        }

        @Override
        public JSONObject toJSONObject()
                throws Exception {
            return new JSONObject(distilledMap());
        }

        public String toJSONString()
                throws Exception {
            return toJSONString(JSONStyle.NO_COMPRESS);
        }

        @Override
        public String toJSONString(JSONStyle style)
                throws Exception {
            JSONObject obj = new JSONObject();
            try {
                obj = new JSONObject(distilledMap());
            } catch (Exception ew) {
                log.error(ERROR, ew);
            }
            return obj.toJSONString(style);
        }
    }

    /**
     * Trigger on event
     *
     * @param eventIterator The event iterator object
     */
    @Override
    public void onEvent(EventIterator eventIterator) {
        ResourceResolver resourceResolver = null;
        try {
            resourceResolver = resourceResolverFactory.getServiceResourceResolver(RESOURCE_RESOLVER_PARAMS);

            ComponentManager componentManager = resourceResolver.adaptTo(ComponentManager.class);
            if (resourceResolver != null) {
                while (eventIterator.hasNext()) {
                    Event event = eventIterator.nextEvent();
                    String path = event.getPath();
                    switch (event.getType()) {
                        case Event.PROPERTY_REMOVED:
                        case Event.NODE_REMOVED:
                        case Event.PROPERTY_CHANGED:
                        case Event.PROPERTY_ADDED:
                            path = path.substring(0, path.lastIndexOf('/'));
                            break;
                        case Event.NODE_MOVED:
                            path = event.getInfo().get("srcChildRelPath").toString();
                            break;
                        case Event.NODE_ADDED:
                        default:
                    }
                    Resource resource = resourceResolver.getResource(path);
                    if (resource != null) {
                        com.day.cq.wcm.api.components.Component component = ResourceUtils.findContainingComponent(resource);
                        if (component != null) {
                            configCache.clear();
                        }
                    }
                }
            }
        } catch (Exception ew) {
            log.error(ERROR, ew);
        } finally {
            if (resourceResolver != null)
                resourceResolver.close();
        }
    }

    private Session session;
    private ObservationManager observationManager;

    /**
     * Component activator
     *
     * @param componentContext The component context
     * @throws Exception
     */
    @Activate
    protected void activate(ComponentContext componentContext)
            throws Exception {
        session = repository.loginService(CONFIG_SERVICE, null);
        observationManager = session.getWorkspace().getObservationManager();
        // set up observation listener
        observationManager.addEventListener(
                this,
                Event.NODE_ADDED | Event.NODE_REMOVED | Event.PROPERTY_CHANGED | Event.PROPERTY_ADDED | Event.PROPERTY_REMOVED,
                APPS_ROOT,
                true /* isDeep */,
                null /* uuid */,
                null /* nodeTypeName */,
                true /* noLocal */
        );
        OSGiUtils.activate(this, componentContext);
    }

    /**
     * Component deactivator
     *
     * @param context The component context
     * @throws Exception
     */
    @Deactivate
    protected void deactivate(final ComponentContext context)
            throws Exception {
        if (session != null) {
            try {
                observationManager.removeEventListener(this);
            } catch (RepositoryException ew) {
                log.error(ERROR, ew);
            }
            session.logout();
            session = null;
        }
    }

    /**
     * Inner class: InertProperty
     */
    private class InertProperty {

        private final int type;
        private final String name, nodePath;
        private final List<Value> values;

        private InertProperty(Property property)
                throws RepositoryException {
            type = property.getType();
            name = property.getName();
            values = (property.isMultiple()) ? Arrays.asList(property.getValues()) : Lists.newArrayList(property.getValue());
            nodePath = property.getParent().getPath();
        }

        private String name() {
            return name;
        }

        private List<Value> values() {
            return values;
        }

        private String nodePath() {
            return nodePath;
        }

        private int type() {
            return type;
        }
    }
}
