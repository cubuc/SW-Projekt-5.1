package kn.uni.inf.sensortagvr.ble;


import java.util.UUID;

import static java.lang.Math.pow;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_ACC_CONF;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_ACC_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_ACC_SERV;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_BAR_CONF;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_BAR_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_BAR_SERV;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_GYR_CONF;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_GYR_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_GYR_SERV;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_HUM_CONF;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_HUM_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_HUM_SERV;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_IRT_CONF;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_IRT_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_IRT_SERV;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_MAG_CONF;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_MAG_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_MAG_SERV;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_MOV_CONF;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_MOV_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_MOV_SERV;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_OPT_CONF;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_OPT_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_OPT_SERV;


enum Sensor {

    IR_TEMPERATURE(UUID.fromString(UUID_IRT_SERV), UUID.fromString(UUID_IRT_DATA), UUID.fromString(UUID_IRT_CONF)) {
        /**
         * @param value
         */
        public float[] convert(final byte[] value) {

			/*
             * The IR Temperature sensor produces two measurements; Object ( AKA target or IR) Temperature, and Ambient ( AKA die ) temperature.
			 * Both need some conversion, and Object temperature is dependent on Ambient temperature.
			 * They are stored as [ObjLSB, ObjMSB, AmbLSB, AmbMSB] (4 bytes) Which means we need to shift the bytes around to get the correct values.
			 */

            float ambient = extractAmbientTemperature(value);
            float target = extractTargetTemperature(value, ambient);
            float targetNewSensor = extractTargetTemperatureTMP007(value);
            return new float[]{ambient, target, targetNewSensor};
        }

        /**
         * @param v
         */
        private float extractAmbientTemperature(byte[] v) {
            int offset = 2;
            return (float) (shortUnsignedAtOffset(v, offset) / 128.0);
        }

        /**
         * @param v
         * @param ambient
         */
        private float extractTargetTemperature(byte[] v, double ambient) {
            Integer twoByteValue = shortSignedAtOffset(v, 0);

            double Vobj2 = twoByteValue.doubleValue();
            Vobj2 *= 0.00000015625;

            double Tdie = ambient + 273.15;

            double S0 = 5.593E-14; // Calibration factor
            double a1 = 1.75E-3;
            double a2 = -1.678E-5;
            double b0 = -2.94E-5;
            double b1 = -5.7E-7;
            double b2 = 4.63E-9;
            double c2 = 13.4;
            double Tref = 298.15;
            double S = S0 * (1 + a1 * (Tdie - Tref) + a2 * pow((Tdie - Tref), 2));
            double Vos = b0 + b1 * (Tdie - Tref) + b2 * pow((Tdie - Tref), 2);
            double fObj = (Vobj2 - Vos) + c2 * pow((Vobj2 - Vos), 2);
            double tObj = pow(pow(Tdie, 4) + (fObj / S), .25);

            return (float) (tObj - 273.15);
        }

        /**
         * @param v
         */
        private float extractTargetTemperatureTMP007(byte[] v) {
            int offset = 0;
            return (float) (shortUnsignedAtOffset(v, offset) / 128.0);
        }
    },

    MOVEMENT_ACC(UUID.fromString(UUID_MOV_SERV), UUID.fromString(UUID_MOV_DATA), UUID.fromString(UUID_MOV_CONF), (byte) 3) {
        /**
         * @param value
         */
        public float[] convert(final byte[] value) {
            // Range 8G
            final float SCALE = (float) 4096.0;

            int x = (value[7] << 8) + value[6];
            int y = (value[9] << 8) + value[8];
            int z = (value[11] << 8) + value[10];
            return new float[]{((x / SCALE) * -1), y / SCALE, ((z / SCALE) * -1)};
        }
    },
    MOVEMENT_GYRO(UUID.fromString(UUID_MOV_SERV), UUID.fromString(UUID_MOV_DATA), UUID.fromString(UUID_MOV_CONF), (byte) 3) {
        /**
         *
         * @param value
         */
        @Override
        public float[] convert(final byte[] value) {

            final float SCALE = (float) 128.0;

            int x = (value[1] << 8) + value[0];
            int y = (value[3] << 8) + value[2];
            int z = (value[5] << 8) + value[4];
            return new float[]{x / SCALE, y / SCALE, z / SCALE};
        }
    },
    MOVEMENT_MAG(UUID.fromString(UUID_MOV_SERV), UUID.fromString(UUID_MOV_DATA), UUID.fromString(UUID_MOV_CONF), (byte) 3) {
        /**
         *
         * @param value
         */
        @Override
        public float[] convert(final byte[] value) {
            final float SCALE = (float) (32768 / 4912);
            if (value.length >= 18) {
                int x = (value[13] << 8) + value[12];
                int y = (value[15] << 8) + value[14];
                int z = (value[17] << 8) + value[16];
                return new float[]{x / SCALE, y / SCALE, z / SCALE};
            } else return new float[]{0, 0, 0};
        }
    },
    ACCELEROMETER(UUID.fromString(UUID_ACC_SERV), UUID.fromString(UUID_ACC_DATA), UUID.fromString(UUID_ACC_CONF), (byte) 3) {
        /**
         *
         * @param value
         */
        @Override
        public float[] convert(final byte[] value) {
            /*
			 * The accelerometer has the range [-2g, 2g] with unit (1/64)g.
			 * To convert from unit (1/64)g to unit g we divide by 64.
			 * (g = 9.81 m/s^2)
			 * The z value is multiplied with -1 to coincide with how we have arbitrarily defined the positive y direction. (illustrated by the apps accelerometer
			 * image)
			 */
            // Range 8G bc we use the 2650STK
            final float SCALE = (float) 4096.0;

            int x = (value[0] << 8) + value[1];
            int y = (value[2] << 8) + value[3];
            int z = (value[4] << 8) + value[5];
            return new float[]{x / SCALE, y / SCALE, z / SCALE};

        }
    },

    HUMIDITY(UUID.fromString(UUID_HUM_SERV), UUID.fromString(UUID_HUM_DATA), UUID.fromString(UUID_HUM_CONF)) {
        /**
         *
         * @param value
         */
        @Override
        public float[] convert(final byte[] value) {
            int a = shortUnsignedAtOffset(value, 2);
            // bits [1..0] are status bits and need to be cleared according
            // to the user guide, but the iOS code doesn't bother. It should
            // have minimal impact.
            a = a - (a % 4);

            return new float[]{(-6f) + 125f * (a / 65535f), 0, 0};
        }
    },
    HUMIDITY2(UUID.fromString(UUID_HUM_SERV), UUID.fromString(UUID_HUM_DATA), UUID.fromString(UUID_HUM_CONF)) {
        /**
         *
         * @param value
         */
        @Override
        public float[] convert(final byte[] value) {
            int a = shortUnsignedAtOffset(value, 2);

            return new float[]{100f * (a / 65535f), 0, 0};
        }
    },
    MAGNETOMETER(UUID.fromString(UUID_MAG_SERV), UUID.fromString(UUID_MAG_DATA), UUID.fromString(UUID_MAG_CONF)) {
        /**
         *
         * @param value
         */
        @Override
        public float[] convert(final byte[] value) {
            // Multiply x and y with -1 so that the values correspond with the image in the app
            float x = shortSignedAtOffset(value, 0) * (2000f / 65536f) * -1;
            float y = shortSignedAtOffset(value, 2) * (2000f / 65536f) * -1;
            float z = shortSignedAtOffset(value, 4) * (2000f / 65536f);

            return new float[]{x, y, z};
        }
    },

    LUXMETER(UUID.fromString(UUID_OPT_SERV), UUID.fromString(UUID_OPT_DATA), UUID.fromString(UUID_OPT_CONF)) {
        /**
         *
         * @param value
         */
        @Override
        public float[] convert(final byte[] value) {
            int mantissa;
            int exponent;
            Integer sfloat = shortUnsignedAtOffset(value, 0);

            mantissa = sfloat & 0x0FFF;
            exponent = (sfloat >> 12) & 0xFF;

            double output;
            double magnitude = pow(2.0f, exponent);
            output = (mantissa * magnitude);

            return new float[]{(float) (output / 100.0f), 0, 0};
        }
    },

    GYROSCOPE(UUID.fromString(UUID_GYR_SERV), UUID.fromString(UUID_GYR_DATA), UUID.fromString(UUID_GYR_CONF), (byte) 7) {
        /**
         *
         * @param value
         */
        @Override
        public float[] convert(final byte[] value) {

            float y = shortSignedAtOffset(value, 0) * (500f / 65536f) * -1;
            float x = shortSignedAtOffset(value, 2) * (500f / 65536f);
            float z = shortSignedAtOffset(value, 4) * (500f / 65536f);

            return new float[]{x, y, z};
        }
    },

    BAROMETER(UUID.fromString(UUID_BAR_SERV), UUID.fromString(UUID_BAR_DATA), UUID.fromString(UUID_BAR_CONF)) {
        /**
         *
         * @param value
         */
        @Override
        public float[] convert(final byte[] value) {

            if (value.length > 4) {
                Integer val = twentyFourBitUnsignedAtOffset(value, 2);
                return new float[]{(float) (val / 100.0), 0, 0};
            } else {
                int mantissa;
                int exponent;
                Integer sfloat = shortUnsignedAtOffset(value, 2);

                mantissa = sfloat & 0x0FFF;
                exponent = (sfloat >> 12) & 0xFF;

                double output;
                double magnitude = pow(2.0f, exponent);
                output = (mantissa * magnitude);
                return new float[]{(float) (output / 100.0f), 0, 0};
            }
        }
    };

    public static final byte DISABLE_SENSOR_CODE = 0;
    public static final byte ENABLE_SENSOR_CODE = 1;
    public static final byte CALIBRATE_SENSOR_CODE = 2;
    public static final Sensor[] SENSOR_LIST = {IR_TEMPERATURE, ACCELEROMETER, MAGNETOMETER, LUXMETER, GYROSCOPE, HUMIDITY, BAROMETER};
    public final UUID service, data, config;
    private byte enableCode; // See getEnableSensorCode for explanation.
    /**
     * Constructor called by the Gyroscope and Accelerometer because it more than a boolean enable
     * code.
     * @param service
     * @param data
     * @param config
     * @param enableCode
     */
    Sensor(UUID service, UUID data, UUID config, byte enableCode) {
        this.service = service;
        this.data = data;
        this.config = config;
        this.enableCode = enableCode;
    }
    /**
     * Constructor called by all the sensors except Gyroscope
     * @param service
     * @param data
     * @param config
     */
    Sensor(UUID service, UUID data, UUID config) {
        this.service = service;
        this.data = data;
        this.config = config;
        this.enableCode = ENABLE_SENSOR_CODE; // This is the sensor enable code for all sensors except the gyroscope
    }

    /**
     *
     * @param uuid
     */
    public static Sensor getSensorFromUuid(UUID uuid) {
        for (Sensor s : Sensor.values()) {
            if (s.getData().equals(uuid) || s.getService().equals(uuid) ||
                    s.getConfig().equals(uuid)) {
                return s;
            }
        }
        throw new RuntimeException("unable to find UUID.");
    }

    /**
     * Gyroscope, Magnetometer, Barometer, IR temperature all store 16 bit two's complement values as LSB MSB, which cannot be directly parsed
     * as getIntValue(FORMAT_SINT16, offset) because the bytes are stored as little-endian.
     *
     * This function extracts these 16 bit two's complement values.
     * @param c
     * @param offset
     */
    private static Integer shortSignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer upperByte = (int) c[offset + 1]; // // Interpret MSB as signed
        return (upperByte << 8) + lowerByte;
    }

    /**
     *
     * @param c
     * @param offset
     */
    private static Integer shortUnsignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer upperByte = (int) c[offset + 1] & 0xFF;
        return (upperByte << 8) + lowerByte;
    }

    /**
     *
     * @param c
     * @param offset
     */
    private static Integer twentyFourBitUnsignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer mediumByte = (int) c[offset + 1] & 0xFF;
        Integer upperByte = (int) c[offset + 2] & 0xFF;
        return (upperByte << 16) + (mediumByte << 8) + lowerByte;
    }

    /**
     *
     */
    public byte getEnableSensorCode() {
        return enableCode;
    }

    /**
     *
     */
    public UUID getService() {
        return service;
    }

    /**
     *
     */
    public UUID getData() {
        return data;
    }

    /**
     *
     */
    public UUID getConfig() {
        return config;
    }

    /**
     * @param value
     */
    public float[] convert(byte[] value) {
        throw new UnsupportedOperationException("Error: the individual enum classes are supposed to override this method.");
    }
}


