package mtn.rso.pricecompare.collectionmanager.services.config;

import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@ConfigBundle("api-properties")
public class ApiProperties {

    @ConfigValue(watch = true)
    private Boolean returnCollectionItemInformation;

    @ConfigValue(watch = true)
    private Boolean verifyItemExists;

    public Boolean getReturnCollectionItemInformation() {
        return returnCollectionItemInformation;
    }

    public void setReturnCollectionItemInformation(Boolean returnCollectionItemInformation) {
        this.returnCollectionItemInformation = returnCollectionItemInformation;
    }

    public Boolean getVerifyItemExists() {
        return verifyItemExists;
    }

    public void setVerifyItemExists(Boolean verifyItemExists) {
        this.verifyItemExists = verifyItemExists;
    }

}
