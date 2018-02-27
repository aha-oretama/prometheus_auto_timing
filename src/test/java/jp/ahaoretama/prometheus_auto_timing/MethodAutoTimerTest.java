package jp.ahaoretama.prometheus_auto_timing;

import static org.junit.Assert.*;

import java.util.Enumeration;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import org.junit.After;
import org.junit.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sekineyasufumi on 2018/02/27.
 */
public class MethodAutoTimerTest {
    int sleep = 1000;

    <T> T getProxy(T source){
        AspectJProxyFactory factory = new AspectJProxyFactory(source);
        factory.addAspect(MethodAutoTimer.class);
        return factory.getProxy();
    }

    @RestController
    public class TestClass {
        public void doSomething() throws Exception {
            Thread.sleep(sleep);
        }
    }

    @RestController
    public class ExceptionClass {
        public void doSomething() throws Exception {
            Thread.sleep(sleep);
            throw new IllegalArgumentException("ExceptionClass method throws new exception.");
        }
    }

    @Test
    public void test_once_request() throws Exception {
        TestClass proxy = getProxy(new TestClass());

        proxy.doSomething();
        final Double count = CollectorRegistry.defaultRegistry.getSampleValue("test_class_do_something_duration_seconds_count");
        final Double metrics = CollectorRegistry.defaultRegistry.getSampleValue("test_class_do_something_duration_seconds_sum");

        assertEquals(1, count,0);
        assertEquals(sleep / 1000, metrics, .1);
    }

    @Test
    public void test_multi_requests() throws Exception {
        TestClass proxy = getProxy(new TestClass());

        proxy.doSomething();
        proxy.doSomething();
        proxy.doSomething();

        final Double count = CollectorRegistry.defaultRegistry.getSampleValue("test_class_do_something_duration_seconds_count");
        final Double metrics = CollectorRegistry.defaultRegistry.getSampleValue("test_class_do_something_duration_seconds_sum");
        assertEquals(3, count,0);
        assertEquals(sleep * 3 / 1000, metrics, .1);
    }

    @Test
    public void test_help_message() throws Exception {
        TestClass proxy = getProxy(new TestClass());

        proxy.doSomething();

        Enumeration<Collector.MetricFamilySamples> metricFamilySamplesEnumeration =
            CollectorRegistry.defaultRegistry.metricFamilySamples();

        Collector.MetricFamilySamples metricFamilySamples = null;

        while (metricFamilySamplesEnumeration.hasMoreElements()) {
            metricFamilySamples = metricFamilySamplesEnumeration.nextElement();
            if(metricFamilySamples.name.equals("test_class_do_something_duration_seconds")) {
                break;
            }
        }

        assertNotNull(metricFamilySamples);
        assertEquals(metricFamilySamples.help, "Duration seconds of TestClass.doSomething");
    }

    @Test
    public void test_exception() throws Exception {
        ExceptionClass proxy = getProxy(new ExceptionClass());
        IllegalArgumentException exception = null;

        try {
            proxy.doSomething();
        }catch (IllegalArgumentException e) {
            exception = e;
        }

        final Double metrics = CollectorRegistry.defaultRegistry.getSampleValue("exception_class_do_something_duration_seconds_sum");
        assertEquals(sleep  / 1000, metrics, .1);
        assertNotNull(exception);
    }


    @After
    public void tearDown() throws Exception {
        CollectorRegistry.defaultRegistry.clear();
    }
}
