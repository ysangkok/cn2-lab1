/**
 *
 */
package com.wifilightsense.pojos;

import java.util.Date;

/**
 * @author FAISAL
 */
public class LightReadings {
    private final Date timestamp;
    private final float lux;

    public LightReadings(Date timestamp, float lux) {
        super();
        this.timestamp = timestamp;
        this.lux = lux;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public float getLux() {
        return lux;
    }


}
