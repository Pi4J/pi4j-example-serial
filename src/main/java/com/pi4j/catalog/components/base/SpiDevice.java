package com.pi4j.catalog.components.base;

import java.time.Duration;

import com.pi4j.context.Context;
import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiConfig;
import com.pi4j.plugin.mock.provider.spi.MockSpi;

public class SpiDevice extends Component {
    /**
     * The PI4J SPI
     */
    private final Spi spi;

    protected SpiDevice(Context pi4j, SpiConfig config){
        spi = pi4j.create(config);
        //give SPI some time to initialize
        delay(Duration.ofSeconds(3));
        logDebug("SPI is ready");
    }

    protected void sendToSerialDevice(byte[] data) {
        spi.write(data);
    }

    @Override
    public void reset() {
        super.reset();
        spi.close();
        logDebug("SPI closed");
    }

    // --------------- for testing --------------------

    public MockSpi mock() {
        return asMock(MockSpi.class, spi);
    }
}
