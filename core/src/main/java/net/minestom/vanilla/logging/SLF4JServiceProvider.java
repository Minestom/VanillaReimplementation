package net.minestom.vanilla.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;

public class SLF4JServiceProvider implements  org.slf4j.spi.SLF4JServiceProvider {

    public SLF4JServiceProvider() {
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return SLF4JCompatibilityLayer::new;
    }

    private final IMarkerFactory markerFactory = new BasicMarkerFactory();
    private final MDCAdapter mdcAdapter = new NOPMDCAdapter();

    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }

    @Override
    public String getRequestedApiVersion() {
        return "2.0";
    }

    @Override
    public void initialize() {
    }
}
