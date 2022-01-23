package com.alilitech.web.support;

import org.springframework.context.support.AbstractMessageSource;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * reloadable message resource
 * @author Zhou Xiaoxiang
 * @since 1.3.10
 */
public class ReloadableMessageResource extends AbstractMessageSource {

    private final Map<Locale, ResourceBundle> localeResourceBundleMap =
            new ConcurrentHashMap<>();

    public void putResourceBundle(Locale locale, ResourceBundle resourceBundle) {
        localeResourceBundleMap.put(locale, resourceBundle);
    }

    public void putResourceBundle(Locale locale, Map<String, Object> resourceMap) {
        localeResourceBundleMap.put(locale, new ReloadableResourceBundle(resourceMap));
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        ResourceBundle bundle = getResourceBundle(locale);
        if (bundle != null) {
            MessageFormat messageFormat = createMessageFormat(bundle.getString(code), locale);
            return messageFormat;
        }
        return null;
    }

    private ResourceBundle getResourceBundle(Locale locale) {
        return localeResourceBundleMap.get(locale);
    }
}
