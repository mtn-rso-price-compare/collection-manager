package mtn.rso.pricecompare.collectionmanager.services.config;

import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@ConfigBundle("client-properties")
public class ClientProperties {

    @ConfigValue(watch = true)
    private String priceUpdaterHost;

    @ConfigValue(watch = true)
    private Integer priceUpdaterPort;

    public String getPriceUpdaterHost() {
        return priceUpdaterHost;
    }

    public void setPriceUpdaterHost(String priceUpdaterHost) {
        this.priceUpdaterHost = priceUpdaterHost;
    }

    public Integer getPriceUpdaterPort() {
        return priceUpdaterPort;
    }

    public void setPriceUpdaterPort(Integer priceUpdaterPort) {
        this.priceUpdaterPort = priceUpdaterPort;
    }
}
