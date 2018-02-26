package jp.ahaoretama.prometheus_auto_timing;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.prometheus.client.Summary;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * @author sekineyasufumi on 2018/02/26.
 */
@Aspect("pertarget(jp.ahaoretama.prometheus_auto_timing.MethodAutoTimer.timeable())")
@Scope("prototype")
@ControllerAdvice
public class MethodAutoTimer {

    private final ReadWriteLock summaryLock = new ReentrantReadWriteLock();
    private final HashMap<String, Summary> summaries = new HashMap();

    public MethodAutoTimer() {
    }

    @Pointcut("within(@org.springframework.stereotype.Controller *)")
    public void beanAnnotatedWithController() {}

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void beanAnnotatedWithRestController() {}


    @Pointcut("execution(public * *(..))")
    public void publicMethod() {}

    @Pointcut("publicMethod() && (beanAnnotatedWithController() || beanAnnotatedWithRestController())")
    public void timeable() {
    }

    private String toSnakeCase(String camelCase) {
        return StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(camelCase), "_")
            .toLowerCase();
    }

    private Summary ensureSummary(ProceedingJoinPoint pjp, String key) throws IllegalStateException {
        Lock writeLock = this.summaryLock.writeLock();
        writeLock.lock();

        Summary var6;
        try {
            Summary summary = this.summaries.get(key);
            if (summary == null) {
                String simpleName = pjp.getTarget().getClass().getSimpleName();
                String methodName = pjp.getSignature().getName();

                String name = StringUtils.join(new String[] {toSnakeCase(simpleName),toSnakeCase(methodName),"duration_seconds"}, "_");
                String help = "Duration seconds of " + simpleName + "." + methodName;

                summary = Summary.build().name(name).help(help).register();
                this.summaries.put(key, summary);
                var6 = summary;
                return var6;
            }

            var6 = summary;
        } finally {
            writeLock.unlock();
        }

        return var6;
    }

    @Around("timeable()")
    public Object timeMethod(ProceedingJoinPoint pjp) throws Throwable {
        String key = pjp.getSignature().toLongString();
        Lock r = this.summaryLock.readLock();
        r.lock();

        Summary summary;
        try {
            summary = this.summaries.get(key);
        } finally {
            r.unlock();
        }

        if (summary == null) {
            summary = this.ensureSummary(pjp, key);
        }

        Summary.Timer t = summary.startTimer();

        Object var6;
        try {
            var6 = pjp.proceed();
        } finally {
            t.observeDuration();
        }

        return var6;
    }

}
