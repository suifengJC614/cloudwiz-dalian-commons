package cn.cloudwiz.dalian.commons.export;

import cn.cloudwiz.dalian.commons.utils.UUIDUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.*;

public class BasicConfig {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private Element config;

    private Map<String, BasicConfig> childs = new LinkedHashMap<>();

    public BasicConfig(Element config) {
        Assert.notNull(config, "config element is null");
        this.config = config;
        this.loadCache();
    }

    public Element getConfigElement() {
        return config;
    }

    protected String getAttribute(String key) {
        return config.attributeValue(key);
    }

    protected String getAttribute(String key, String defvalue) {
        return config.attributeValue(key, defvalue);
    }

    public String getElementName() {
        return config.getName();
    }

    public String getContent() {
        return config.getStringValue();
    }

    public List<BasicConfig> getChildConfigs() {
        return new ArrayList<>(childs.values());
    }

    public BasicConfig getChildConfig(String id) {
        return childs.get(id);
    }

    @SuppressWarnings("unchecked")
    protected void loadCache() {
        String childName = getChildElementName();
        Iterator<Element> iterator;
        if (StringUtils.isNotEmpty(childName)) {
            iterator = config.elementIterator(childName);
        } else {
            iterator = config.elementIterator();
        }
        int index = 0;
        iterator.forEachRemaining(item -> {
            String id = item.attributeValue("id");
            if (id == null) {
                id = item.attributeValue("name");
            }
            if (id == null) {
                id = "child_" + UUIDUtils.randomUUID();
            }
            childs.put(id, createChildConfig(item));
        });
    }

    protected BasicConfig createChildConfig(Element element) {
        return new BasicConfig(element);
    }

    protected String getChildElementName() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (!(obj instanceof BasicConfig)) return false;
        return Objects.equals(config, ((BasicConfig) obj).config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(config);
    }

}
