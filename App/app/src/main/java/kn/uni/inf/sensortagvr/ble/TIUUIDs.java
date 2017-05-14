package kn.uni.inf.sensortagvr.ble;
import static java.util.UUID.fromString;
import java.util.UUID;


public class TIUUIDs {
    //TODO LEDs

    public final static String
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

    public static final String[] UUID_DATA = {UUID_IRT_DATA, UUID_ACC_DATA, UUID_HUM_DATA, UUID_MAG_DATA, UUID_OPT_DATA, UUID_BAR_DATA, UUID_GYR_DATA, UUID_MOV_DATA};
}

