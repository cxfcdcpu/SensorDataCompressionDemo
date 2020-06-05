/**
 * This class is automatically generated by mig. DO NOT EDIT THIS FILE.
 * This class implements a Java interface to the 'baseControlMsg'
 * message type.
 */

public class baseControlMsg extends net.tinyos.message.Message {

    /** The default size of this message type in bytes. */
    public static final int DEFAULT_MESSAGE_SIZE = 7;

    /** The Active Message type associated with this message. */
    public static final int AM_TYPE = -1;

    /** Create a new baseControlMsg of size 7. */
    public baseControlMsg() {
        super(DEFAULT_MESSAGE_SIZE);
        amTypeSet(AM_TYPE);
    }

    /** Create a new baseControlMsg of the given data_length. */
    public baseControlMsg(int data_length) {
        super(data_length);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new baseControlMsg with the given data_length
     * and base offset.
     */
    public baseControlMsg(int data_length, int base_offset) {
        super(data_length, base_offset);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new baseControlMsg using the given byte array
     * as backing store.
     */
    public baseControlMsg(byte[] data) {
        super(data);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new baseControlMsg using the given byte array
     * as backing store, with the given base offset.
     */
    public baseControlMsg(byte[] data, int base_offset) {
        super(data, base_offset);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new baseControlMsg using the given byte array
     * as backing store, with the given base offset and data length.
     */
    public baseControlMsg(byte[] data, int base_offset, int data_length) {
        super(data, base_offset, data_length);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new baseControlMsg embedded in the given message
     * at the given base offset.
     */
    public baseControlMsg(net.tinyos.message.Message msg, int base_offset) {
        super(msg, base_offset, DEFAULT_MESSAGE_SIZE);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new baseControlMsg embedded in the given message
     * at the given base offset and length.
     */
    public baseControlMsg(net.tinyos.message.Message msg, int base_offset, int data_length) {
        super(msg, base_offset, data_length);
        amTypeSet(AM_TYPE);
    }

    /**
    /* Return a String representation of this message. Includes the
     * message type name and the non-indexed field values.
     */
    public String toString() {
      String s = "Message <baseControlMsg> \n";
      try {
        s += "  [version=0x"+Long.toHexString(get_version())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [settingOrControl=0x"+Long.toHexString(get_settingOrControl())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [interval=0x"+Long.toHexString(get_interval())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [mode=0x"+Long.toHexString(get_mode())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [start=0x"+Long.toHexString(get_start())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      return s;
    }

    // Message-type-specific access methods appear below.

    /////////////////////////////////////////////////////////
    // Accessor methods for field: version
    //   Field type: int, unsigned
    //   Offset (bits): 0
    //   Size (bits): 16
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'version' is signed (false).
     */
    public static boolean isSigned_version() {
        return false;
    }

    /**
     * Return whether the field 'version' is an array (false).
     */
    public static boolean isArray_version() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'version'
     */
    public static int offset_version() {
        return (0 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'version'
     */
    public static int offsetBits_version() {
        return 0;
    }

    /**
     * Return the value (as a int) of the field 'version'
     */
    public int get_version() {
        return (int)getUIntBEElement(offsetBits_version(), 16);
    }

    /**
     * Set the value of the field 'version'
     */
    public void set_version(int value) {
        setUIntBEElement(offsetBits_version(), 16, value);
    }

    /**
     * Return the size, in bytes, of the field 'version'
     */
    public static int size_version() {
        return (16 / 8);
    }

    /**
     * Return the size, in bits, of the field 'version'
     */
    public static int sizeBits_version() {
        return 16;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: settingOrControl
    //   Field type: short, unsigned
    //   Offset (bits): 16
    //   Size (bits): 8
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'settingOrControl' is signed (false).
     */
    public static boolean isSigned_settingOrControl() {
        return false;
    }

    /**
     * Return whether the field 'settingOrControl' is an array (false).
     */
    public static boolean isArray_settingOrControl() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'settingOrControl'
     */
    public static int offset_settingOrControl() {
        return (16 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'settingOrControl'
     */
    public static int offsetBits_settingOrControl() {
        return 16;
    }

    /**
     * Return the value (as a short) of the field 'settingOrControl'
     */
    public short get_settingOrControl() {
        return (short)getUIntBEElement(offsetBits_settingOrControl(), 8);
    }

    /**
     * Set the value of the field 'settingOrControl'
     */
    public void set_settingOrControl(short value) {
        setUIntBEElement(offsetBits_settingOrControl(), 8, value);
    }

    /**
     * Return the size, in bytes, of the field 'settingOrControl'
     */
    public static int size_settingOrControl() {
        return (8 / 8);
    }

    /**
     * Return the size, in bits, of the field 'settingOrControl'
     */
    public static int sizeBits_settingOrControl() {
        return 8;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: interval
    //   Field type: int, unsigned
    //   Offset (bits): 24
    //   Size (bits): 16
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'interval' is signed (false).
     */
    public static boolean isSigned_interval() {
        return false;
    }

    /**
     * Return whether the field 'interval' is an array (false).
     */
    public static boolean isArray_interval() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'interval'
     */
    public static int offset_interval() {
        return (24 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'interval'
     */
    public static int offsetBits_interval() {
        return 24;
    }

    /**
     * Return the value (as a int) of the field 'interval'
     */
    public int get_interval() {
        return (int)getUIntBEElement(offsetBits_interval(), 16);
    }

    /**
     * Set the value of the field 'interval'
     */
    public void set_interval(int value) {
        setUIntBEElement(offsetBits_interval(), 16, value);
    }

    /**
     * Return the size, in bytes, of the field 'interval'
     */
    public static int size_interval() {
        return (16 / 8);
    }

    /**
     * Return the size, in bits, of the field 'interval'
     */
    public static int sizeBits_interval() {
        return 16;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: mode
    //   Field type: short, unsigned
    //   Offset (bits): 40
    //   Size (bits): 8
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'mode' is signed (false).
     */
    public static boolean isSigned_mode() {
        return false;
    }

    /**
     * Return whether the field 'mode' is an array (false).
     */
    public static boolean isArray_mode() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'mode'
     */
    public static int offset_mode() {
        return (40 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'mode'
     */
    public static int offsetBits_mode() {
        return 40;
    }

    /**
     * Return the value (as a short) of the field 'mode'
     */
    public short get_mode() {
        return (short)getUIntBEElement(offsetBits_mode(), 8);
    }

    /**
     * Set the value of the field 'mode'
     */
    public void set_mode(short value) {
        setUIntBEElement(offsetBits_mode(), 8, value);
    }

    /**
     * Return the size, in bytes, of the field 'mode'
     */
    public static int size_mode() {
        return (8 / 8);
    }

    /**
     * Return the size, in bits, of the field 'mode'
     */
    public static int sizeBits_mode() {
        return 8;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: start
    //   Field type: short, unsigned
    //   Offset (bits): 48
    //   Size (bits): 8
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'start' is signed (false).
     */
    public static boolean isSigned_start() {
        return false;
    }

    /**
     * Return whether the field 'start' is an array (false).
     */
    public static boolean isArray_start() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'start'
     */
    public static int offset_start() {
        return (48 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'start'
     */
    public static int offsetBits_start() {
        return 48;
    }

    /**
     * Return the value (as a short) of the field 'start'
     */
    public short get_start() {
        return (short)getUIntBEElement(offsetBits_start(), 8);
    }

    /**
     * Set the value of the field 'start'
     */
    public void set_start(short value) {
        setUIntBEElement(offsetBits_start(), 8, value);
    }

    /**
     * Return the size, in bytes, of the field 'start'
     */
    public static int size_start() {
        return (8 / 8);
    }

    /**
     * Return the size, in bits, of the field 'start'
     */
    public static int sizeBits_start() {
        return 8;
    }

}