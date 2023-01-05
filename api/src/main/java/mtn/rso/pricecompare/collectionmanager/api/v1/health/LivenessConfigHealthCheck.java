package mtn.rso.pricecompare.collectionmanager.api.v1.health;

import mtn.rso.pricecompare.collectionmanager.services.config.GlobalProperties;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Liveness
@ApplicationScoped
public class LivenessConfigHealthCheck implements HealthCheck {

    @Inject
    private GlobalProperties globalProperties;

    @Override
    public HealthCheckResponse call() {
        if (globalProperties.getAppLiveness())
            return HealthCheckResponse.up(LivenessConfigHealthCheck.class.getSimpleName());
        else
            return HealthCheckResponse.down(LivenessConfigHealthCheck.class.getSimpleName());
    }

}
