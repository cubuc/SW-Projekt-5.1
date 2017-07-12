package kn.uni.inf.sensortagvr.ble;


import java.util.UUID;

import static java.lang.Math.pow;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_BAR_CONF;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_BAR_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_BAR_SERV;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_HUM_CONF;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_HUM_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_HUM_SERV;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_IRT_CONF;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_IRT_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_IRT_SERV;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_OPT_CONF;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_OPT_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_OPT_SERV;


public enum Sensor {

    IR_TEMPERATURE("Temperature", UUID.fromString(UUID_IRT_SERV), UUID.fromString(UUID_IRT_DATA), UUID.fromString(UUID_IRT_CONF)) {
        /**
         * converts the raw data to a float[3]
         *
         * @param value byte measured by the TI CC2650 MCU temperature sensor
         */
        @Override
        public float convert(final byte[] value) {

			/*
             * The IR Temperature sensor produces two measurements; Object ( AKA target or IR) Temperature, and Ambient ( AKA die ) temperature.
			 * Both need some conversion, and Object temperature is dependent on Ambient temperature.
			 * They are stored as [ObjLSB, ObjMSB, AmbLSB, AmbMSB] (4 bytes) Which means we need to shift the bytes around to get the correct values.
			 */
            int offset = 0;
            return (float) (shortUnsignedAtOffset(value, offset) / 128.0);
        }
    },



    HUMIDITY("Humidity Sensor", UUID.fromString(UUID_HUM_SERV), UUID.fromString(UUID_HUM_DATA), UUID.fromString(UUID_HUM_CONF)) {

        /**
         *
         * @param value byte measured by the TI CC2650 MCU humidity sensor
         */
        @Override
        public float convert(final byte[] value) {
            int a = shortUnsignedAtOffset(value, 2);
            // bits [1..0] are status bits and need to be cleared according
            // to the user guide, but the iOS code doesn't bother. It should
            // have minimal impact.
            a = a - (a % 4);

            return (-6f) + 125f * (a / 65535f);
        }
    },


    LUXMETER("Luxmeter", UUID.fromString(UUID_OPT_SERV), UUID.fromString(UUID_OPT_DATA), UUID.fromString(UUID_OPT_CONF)) {
        /**
         *
         * @param value byte received by the TI CC2650 MCU luxmeter
         */
        @Override
        public float convert(final byte[] value) {
            int mantissa;
            int exponent;
            Integer sfloat = shortUnsignedAtOffset(value, 0);

            mantissa = sfloat & 0x0FFF;
            exponent = (sfloat >> 12) & 0xFF;

            double output;
            double magnitude = pow(2.0f, exponent);
            output = (mantissa * magnitude);

            return (float) (output / 100.0f);
        }
    },


    BAROMETER("Barometer", UUID.fromString(UUID_BAR_SERV), UUID.fromString(UUID_BAR_DATA), UUID.fromString(UUID_BAR_CONF)) {
        /**
         * @param value byte received by the TI CC2650 MCU barometer
         */
        @Override
        public float convert(final byte[] value) {

            if (value.length > 4) {
                Integer val = twentyFourBitUnsignedAtOffset(value, 2);
                return (float) (val / 100.0);
            } else {
                int mantissa;
                int exponent;
                Integer sfloat = shortUnsignedAtOffset(value, 2);

                mantissa = sfloat & 0x0FFF;
                exponent = (sfloat >> 12) & 0xFF;

                double output;
                double magnitude = pow(2.0f, exponent);
                output = (mantissa * magnitude);
                return (float) (output / 100.0f);
            }
        }

        /**
         * @param c      some byte
         * @param offset some offset (always 2)
         */
        private Integer twentyFourBitUnsignedAtOffset(byte[] c, int offset) {
            Integer lowerByte = (int) c[offset] & 0xFF;
            Integer mediumByte = (int) c[offset + 1] & 0xFF;
            Integer upperByte = (int) c[offset + 2] & 0xFF;
            return (upperByte << 16) + (mediumByte << 8) + lowerByte;
        }
    };


    protected static final Sensor[] SENSOR_LIST = {IR_TEMPERATURE, LUXMETER, HUMIDITY, BAROMETER};
    private static final byte ENABLE_SENSOR_CODE = 1;
    private final String name;
    private final UUID service;
    private final UUID data;
    private final UUID config;
    private final byte enableCode; // See getEnableSensorCode for explanation.

    /**
     * Constructor called by all the sensors except Gyroscope
     * @param name the name of the according sensor
     * @param service The UUID of the GATT Service of the sensor
     * @param data The UUID of the Characteristic of the sensor data
     * @param config The UUID of the configuration characteristic
     */
    Sensor(String name, UUID service, UUID data, UUID config) {
        this.name = name;
        this.service = service;
        this.data = data;
        this.config = config;
        this.enableCode = ENABLE_SENSOR_CODE;
        // This is the sensor enable code for all sensors except the gyroscope
    }

    /**
     *
     * @param c some byte
     * @param offset some offset
     */
    private static Integer shortUnsignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer upperByte = (int) c[offset + 1] & 0xFF;
        return (upperByte << 8) + lowerByte;
    }


    public byte getEnableSensorCode() {
        return enableCode;
    }


    public UUID getServiceUUID() {
        return service;
    }


    public UUID getDataUUID() {
        return data;
    }


    public UUID getConfigUUID() {
        return config;
    }

    public String getName() {
        return name;
    }

    /** @param value raw characteristic data */
    public float convert(byte[] value) {
        throw new UnsupportedOperationException("Error: the individual enum classes are supposed to override this method.");
    }

}




