package kn.uni.inf.sensortagvr.ble;


import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

/**
 *
 */
class TIUUIDs {
    //TODO LEDs

    final static String
            UUID_DEVINFO_SERV = "0000180a-0000-1000-8000-00805f9b34fb",
            UUID_DEVINFO_FWREV = "00002A26-0000-1000-8000-00805f9b34fb",
            UUID_CCC = "00002902-0000-1000-8000-00805f9b34fb",

    UUID_IRT_SERV = "f000aa00-0451-4000-b000-000000000000",
            UUID_IRT_DATA = "f000aa01-0451-4000-b000-000000000000",
            UUID_IRT_CONF = "f000aa02-0451-4000-b000-000000000000", // 0: disable, 1: enable
            UUID_IRT_PERI = "f000aa03-0451-4000-b000-000000000000", // Period in tens of milliseconds

    UUID_ACC_SERV = "f000aa10-0451-4000-b000-000000000000",
            UUID_ACC_DATA = "f000aa11-0451-4000-b000-000000000000",
            UUID_ACC_CONF = "f000aa12-0451-4000-b000-000000000000", // 0: disable, 1: enable
            UUID_ACC_PERI = "f000aa13-0451-4000-b000-000000000000", // Period in tens of milliseconds

    UUID_HUM_SERV = "f000aa20-0451-4000-b000-000000000000",
            UUID_HUM_DATA = "f000aa21-0451-4000-b000-000000000000",
            UUID_HUM_CONF = "f000aa22-0451-4000-b000-000000000000", // 0: disable, 1: enable
            UUID_HUM_PERI = "f000aa23-0451-4000-b000-000000000000", // Period in tens of milliseconds

    UUID_MAG_SERV = "f000aa30-0451-4000-b000-000000000000",
            UUID_MAG_DATA = "f000aa31-0451-4000-b000-000000000000",
            UUID_MAG_CONF = "f000aa32-0451-4000-b000-000000000000", // 0: disable, 1: enable
            UUID_MAG_PERI = "f000aa33-0451-4000-b000-000000000000", // Period in tens of milliseconds

    UUID_OPT_SERV = "f000aa70-0451-4000-b000-000000000000",
            UUID_OPT_DATA = "f000aa71-0451-4000-b000-000000000000",
            UUID_OPT_CONF = "f000aa72-0451-4000-b000-000000000000", // 0: disable, 1: enable
            UUID_OPT_PERI = "f000aa73-0451-4000-b000-000000000000", // Period in tens of milliseconds

    UUID_BAR_SERV = "f000aa40-0451-4000-b000-000000000000",
            UUID_BAR_DATA = "f000aa41-0451-4000-b000-000000000000",
            UUID_BAR_CONF = "f000aa42-0451-4000-b000-000000000000", // 0: disable, 1: enable
            UUID_BAR_CALI = "f000aa43-0451-4000-b000-000000000000", // Calibration characteristic
            UUID_BAR_PERI = "f000aa44-0451-4000-b000-000000000000", // Period in tens of milliseconds

    UUID_GYR_SERV = "f000aa50-0451-4000-b000-000000000000",
            UUID_GYR_DATA = "f000aa51-0451-4000-b000-000000000000",
            UUID_GYR_CONF = "f000aa52-0451-4000-b000-000000000000", // 0: disable, bit 0: enable x, bit 1: enable y, bit 2: enable z
            UUID_GYR_PERI = "f000aa53-0451-4000-b000-000000000000", // Period in tens of milliseconds

    UUID_MOV_SERV = "f000aa80-0451-4000-b000-000000000000",
            UUID_MOV_DATA = "f000aa81-0451-4000-b000-000000000000",
            UUID_MOV_CONF = "f000aa82-0451-4000-b000-000000000000", // 0: disable, bit 0: enable x, bit 1: enable y, bit 2: enable z
            UUID_MOV_PERI = "f000aa83-0451-4000-b000-000000000000", // Period in tens of milliseconds

    UUID_TST_SERV = "f000aa64-0451-4000-b000-000000000000",
            UUID_TST_DATA = "f000aa65-0451-4000-b000-000000000000", // Test result

    UUID_KEY_SERV = "0000ffe0-0000-1000-8000-00805f9b34fb",
            UUID_KEY_DATA = "0000ffe1-0000-1000-8000-00805f9b34fb";
    static HashSet<UUID> config = new HashSet<>();
    static HashSet<UUID> data = new HashSet<>();
    private static HashMap<String, String> attributes = new HashMap<>();

    static {
        // Services
        attributes.put(UUID_DEVINFO_SERV, "Device Info Service");
        attributes.put(UUID_IRT_SERV, "Temperature Sensor Service");
        attributes.put(UUID_ACC_SERV, "Accelerometer Service");
        attributes.put(UUID_HUM_SERV, "Humidity Sensor Service");
        attributes.put(UUID_MAG_SERV, "Magnetometer Service");
        attributes.put(UUID_OPT_SERV, "Luxmeter Service");
        attributes.put(UUID_BAR_SERV, "Barometer Service");
        attributes.put(UUID_GYR_SERV, "Gyrometer Service");
        attributes.put(UUID_MOV_SERV, "Movement Sensor Service");
        attributes.put(UUID_TST_SERV, "Test Service");
        attributes.put(UUID_KEY_SERV, "Key ? Service");

        // Characteristics
        // Data
        attributes.put(UUID_DEVINFO_FWREV, "Firmware Revision");
        attributes.put(UUID_IRT_DATA, "Temperature Sensor Data");
        attributes.put(UUID_ACC_DATA, "Accelerometer Data");
        attributes.put(UUID_HUM_DATA, "Humidity Sensor Data");
        attributes.put(UUID_MAG_DATA, "Magnetometer Data");
        attributes.put(UUID_OPT_DATA, "Luxmeter Data");
        attributes.put(UUID_BAR_DATA, "Barometer Data");
        attributes.put(UUID_GYR_DATA, "Gyrometer Data");
        attributes.put(UUID_MOV_DATA, "Movement Sensor Data");
        attributes.put(UUID_TST_DATA, "Test Data");
        attributes.put(UUID_KEY_DATA, " Key Data?");

        // Update intervals
        attributes.put(UUID_IRT_PERI, "Temperature Sensor Update Interval");
        attributes.put(UUID_ACC_PERI, "Accelerometer Update Interval");
        attributes.put(UUID_HUM_PERI, "Humidity Sensor Update Interval");
        attributes.put(UUID_MAG_PERI, "Magnetometer Update Interval");
        attributes.put(UUID_OPT_PERI, "Luxmeter Update Interval");
        attributes.put(UUID_BAR_PERI, "Barometer Update Interval");
        attributes.put(UUID_GYR_PERI, "Gyrometer Update Interval");
        attributes.put(UUID_MOV_PERI, "Movement Sensor Update Interval");

        // Conf
        attributes.put(UUID_CCC, "Client Characteristics Config");
        attributes.put(UUID_IRT_CONF, "Temperature Sensor Config");
        attributes.put(UUID_ACC_CONF, "Accelerometer Config");
        attributes.put(UUID_HUM_CONF, "Humidity Config");
        attributes.put(UUID_MAG_CONF, "Magnetometer Config");
        attributes.put(UUID_OPT_CONF, "Luxmeter Config");
        attributes.put(UUID_BAR_CONF, "Barometer Config");
        attributes.put(UUID_BAR_CALI, "Barometer Calibration");
        attributes.put(UUID_GYR_CONF, "Gyrometer Config");
        attributes.put(UUID_MOV_CONF, "Movement Sensor Config");
    }

    static {
        config.add(UUID.fromString(UUID_IRT_CONF));
        config.add(UUID.fromString(UUID_ACC_CONF));
        config.add(UUID.fromString(UUID_HUM_CONF));
        config.add(UUID.fromString(UUID_MAG_CONF));
        config.add(UUID.fromString(UUID_OPT_CONF));
        config.add(UUID.fromString(UUID_BAR_CONF));
        config.add(UUID.fromString(UUID_GYR_CONF));
        config.add(UUID.fromString(UUID_MOV_CONF));
    }

    static {
        data.add(UUID.fromString(UUID_IRT_DATA));
        data.add(UUID.fromString(UUID_ACC_DATA));
        data.add(UUID.fromString(UUID_HUM_DATA));
        data.add(UUID.fromString(UUID_MAG_DATA));
        data.add(UUID.fromString(UUID_OPT_DATA));
        data.add(UUID.fromString(UUID_BAR_DATA));
        data.add(UUID.fromString(UUID_GYR_DATA));
        data.add(UUID.fromString(UUID_MOV_DATA));
    }

    /**
     * @param uuid
     * @param defaultName
     */
    static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}


