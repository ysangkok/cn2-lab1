/**
 *
 */
package com.wifilightsense.pojos;

/**
 * @author FAISAL
 */
public class LightReadings {
    private final Long timestamp;
    private final float lux;

    public LightReadings(Long timestamp, float lux) {
        super();
        this.timestamp = timestamp;
        this.lux = lux;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public float getLux() {
        return lux;
    }


}
