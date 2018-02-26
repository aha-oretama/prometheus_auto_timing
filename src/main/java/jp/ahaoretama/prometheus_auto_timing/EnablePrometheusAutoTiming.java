package jp.ahaoretama.prometheus_auto_timing;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * @author sekineyasufumi on 2018/02/26.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Import({MethodAutoTimer.class})
@Documented
public @interface EnablePrometheusAutoTiming {
}
