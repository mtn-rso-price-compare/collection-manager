package mtn.rso.pricecompare.collectionmanager.api.v1.interceptors;

import com.kumuluz.ee.common.runtime.EeRuntime;
import com.kumuluz.ee.logs.cdi.Log;
import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.core.util.UuidUtil;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.util.HashMap;

@Log
@Interceptor
@Priority(Interceptor.Priority.PLATFORM_BEFORE)
public class LogContextInterceptor {

    @AroundInvoke
    public Object logMethodEntryAndExit(InvocationContext context) throws Exception {

        HashMap<String, String> logContext = new HashMap<>();

        Config config = ConfigProvider.getConfig();

        logContext.put("applicationName", config.getValue("kumuluzee.name", String.class));
        logContext.put("applicationVersion", config.getValue("kumuluzee.version", String.class));
        logContext.put("environmentType", config.getValue("kumuluzee.env.name", String.class));
        logContext.put("uniqueInstanceId", EeRuntime.getInstance().getInstanceId());
        logContext.put("uniqueRequestId", UuidUtil.getTimeBasedUuid().toString());

        try (final CloseableThreadContext.Instance ignored = CloseableThreadContext.putAll(logContext)) {
            return context.proceed();
        }
    }
}
