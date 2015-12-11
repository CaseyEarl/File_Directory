//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.util.StringTokenizer;

public class SysLib {
    public SysLib() {
    }

    public static int exec(String[] var0) {
        return Kernel.interrupt(1, 1, 0, var0);
    }

    public static int join() {
        return Kernel.interrupt(1, 2, 0, (Object)null);
    }

    public static int boot() {
        return Kernel.interrupt(1, 0, 0, (Object)null);
    }

    public static int exit() {
        return Kernel.interrupt(1, 3, 0, (Object)null);
    }

    public static int sleep(int var0) {
        return Kernel.interrupt(1, 4, var0, (Object)null);
    }

    public static int disk() {
        return Kernel.interrupt(2, 0, 0, (Object)null);
    }

    public static int cin(StringBuffer var0) {
        return Kernel.interrupt(1, 8, 0, var0);
    }

    public static int cout(String var0) {
        return Kernel.interrupt(1, 9, 1, var0);
    }

    public static int cerr(String var0) {
        return Kernel.interrupt(1, 9, 2, var0);
    }

    public static int rawread(int var0, byte[] var1) {
        return Kernel.interrupt(1, 5, var0, var1);
    }

    public static int rawwrite(int var0, byte[] var1) {
        return Kernel.interrupt(1, 6, var0, var1);
    }

    public static int sync() {
        return Kernel.interrupt(1, 7, 0, (Object)null);
    }

    public static int cread(int var0, byte[] var1) {
        return Kernel.interrupt(1, 10, var0, var1);
    }

    public static int cwrite(int var0, byte[] var1) {
        return Kernel.interrupt(1, 11, var0, var1);
    }

    public static int flush() {
        return Kernel.interrupt(1, 13, 0, (Object)null);
    }

    public static int csync() {
        return Kernel.interrupt(1, 12, 0, (Object)null);
    }

    public static String[] stringToArgs(String var0) {
        StringTokenizer var1 = new StringTokenizer(var0, " ");
        String[] var2 = new String[var1.countTokens()];

        for(int var3 = 0; var1.hasMoreTokens(); ++var3) {
            var2[var3] = var1.nextToken();
        }

        return var2;
    }

    public static void short2bytes(short var0, byte[] var1, int var2) {
        var1[var2] = (byte)(var0 >> 8);
        var1[var2 + 1] = (byte)var0;
    }

    public static short bytes2short(byte[] var0, int var1) {
        byte var2 = 0;
        short var3 = (short)(var2 + (var0[var1] & 255));
        var3 = (short)(var3 << 8);
        var3 = (short)(var3 + (var0[var1 + 1] & 255));
        return var3;
    }

    public static void int2bytes(int var0, byte[] var1, int var2) {
        var1[var2] = (byte)(var0 >> 24);
        var1[var2 + 1] = (byte)(var0 >> 16);
        var1[var2 + 2] = (byte)(var0 >> 8);
        var1[var2 + 3] = (byte)var0;
    }

    public static int bytes2int(byte[] var0, int var1) {
        int var2 = ((var0[var1] & 255) << 24) + ((var0[var1 + 1] & 255) << 16) + ((var0[var1 + 2] & 255) << 8) + (var0[var1 + 3] & 255);
        return var2;
    }

    public static int format(int var0) {
        return Kernel.interrupt(1, 18, var0, (Object)null);
    }

    public static int open(String var0, String var1) {
        String[] var2 = new String[]{var0, var1};
        return Kernel.interrupt(1, 14, 0, var2);
    }

    public static int close(int var0) {
        return Kernel.interrupt(1, 15, var0, (Object)null);
    }

    public static int read(int var0, byte[] var1) {

        return Kernel.interrupt(1, 8, var0, var1);
    }

    public static int write(int var0, byte[] var1) {
        return Kernel.interrupt(1, 9, var0, var1);
    }

    public static int seek(int var0, int var1, int var2) {
        int[] var3 = new int[]{var1, var2};
        return Kernel.interrupt(1, 17, var0, var3);
    }

    public static int fsize(int var0) {
        return Kernel.interrupt(1, 16, var0, (Object)null);
    }

    public static int delete(String var0) {
        return Kernel.interrupt(1, 19, 0, var0);
    }
}
