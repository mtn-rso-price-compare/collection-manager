package mtn.rso.pricecompare.collectionmanager.services.config;

import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@ConfigBundle("global-properties")
public class GlobalProperties {

    @ConfigValue(watch = true)
    private Boolean appLiveness;

    @ConfigValue(watch = true)
    private Boolean appReadiness;

    public Boolean getAppLiveness() {
        return appLiveness;
    }

    public void setAppLiveness(Boolean appLiveness) {
        this.appLiveness = appLiveness;
    }

    public Boolean getAppReadiness() {
        return appReadiness;
    }

    public void setAppReadiness(Boolean appReadiness) {
        this.appReadiness = appReadiness;
    }
}
