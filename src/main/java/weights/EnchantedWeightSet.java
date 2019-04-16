package weights;

import sun.misc.Unsafe;

import java.io.Serializable;
import java.lang.reflect.Field;

public class EnchantedWeightSet extends WeightsSet implements Serializable {

    private static transient long longArrayHeader;
    private static transient long byteArrayHeader;

    private static transient final long HEADER_ARRAY_LENGTH = 0xFFFFFFFF00000000L;
    private static transient final long HEADER_CELL_TYPE =    0x00000000FFFFFFFFL;

    private static transient Unsafe unsafe;

    private transient long[] longs;

    public EnchantedWeightSet(long[] longArray) {
        this.longs = longArray;
    }

    static {
        try {

            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);

            unsafe = (Unsafe) field.get(null);

            longArrayHeader = unsafe.getLong(new long[0], 8L);
            byteArrayHeader = unsafe.getLong(new byte[0], 8L);

        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.defaultWriteObject();

        long longArrayHeader = unsafe.getLong(longs, 8L);

        long newHeader = 0L;
        //change length (*8)
        newHeader |= (longArrayHeader & HEADER_ARRAY_LENGTH) << 3;

        //change type
        newHeader |= byteArrayHeader & HEADER_CELL_TYPE;


        try{
            unsafe.putLong(longs, 8L, newHeader);

            final Object temp = longs;
            final byte[] newArr = (byte[]) temp;

            s.writeObject(newArr);
        } finally {
            unsafe.putLong(longs, 8L, longArrayHeader);
        }
    }

    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();

        byte[] unsafeBytes = (byte[]) s.readObject();
        long byteArrayHeader = unsafe.getLong(unsafeBytes, 8L);

        long newHeader = 0L;
        //change length (*8)
        newHeader |= (byteArrayHeader & HEADER_ARRAY_LENGTH) >> 3;

        //change type
        newHeader |= longArrayHeader & HEADER_CELL_TYPE;

        unsafe.putLong(unsafeBytes, 8L, newHeader);

        final Object temp = unsafeBytes;
        longs = (long[]) temp;
    }
}