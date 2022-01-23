package com.alilitech.web.support;

import sun.util.ResourceBundleEnumeration;

import java.util.Enumeration;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * reloadable resource bundle
 * @author Zhou Xiaoxiang
 * @since 1.3.10
 */
public class ReloadableResourceBundle extends ResourceBundle {

    public ReloadableResourceBundle(Map<String, Object> lookup) {
        this.lookup = lookup;
    }

    public void putAll(Map<String, Object> lookup) {
        this.lookup = lookup;
    }

    @Override
    protected Object handleGetObject(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return lookup.get(key);
    }

    @Override
    public Enumeration<String> getKeys() {
        return new ResourceBundleEnumeration(lookup.keySet(), null);
    }

    // ==================privates====================
    private Map<String, Object> lookup;
}
