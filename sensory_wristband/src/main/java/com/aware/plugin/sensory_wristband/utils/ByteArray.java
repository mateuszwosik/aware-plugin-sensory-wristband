package com.aware.plugin.sensory_wristband.utils;

public class ByteArray {

    /**
     * Merge two byte arrays.
     * @param array1 - First byte array
     * @param array2 - Second byte array
     * @return merged byte array
     */
    public static byte[] merge(byte[] array1, byte[] array2){
        int length = array1.length + array2.length;
        byte[] result = new byte[length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    /**
     * Merge three byte arrays.
     * @param array1 - First byte array
     * @param array2 - Second byte array
     * @param array3 - Third byte array
     * @return merged byte array
     */
    public static byte[] merge(byte[] array1, byte[] array2, byte[] array3){
        return merge(merge(array1,array2),array3);
    }

}
