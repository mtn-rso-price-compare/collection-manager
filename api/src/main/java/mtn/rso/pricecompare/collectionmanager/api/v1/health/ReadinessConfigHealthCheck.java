package mtn.rso.pricecompare.collectionmanager.api.v1.health;

import mtn.rso.pricecompare.collectionmanager.services.config.GlobalProperties;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Readiness
@ApplicationScoped
public class ReadinessConfigHealthCheck implements HealthCheck {

    @Inject
    private GlobalProperties globalProperties;

    @Override
    public HealthCheckResponse call() {
        if (globalProperties.getAppReadiness())
            return HealthCheckResponse.up(ReadinessConfigHealthCheck.class.getSimpleName());
        else
            return HealthCheckResponse.down(ReadinessConfigHealthCheck.class.getSimpleName());
    }

}
