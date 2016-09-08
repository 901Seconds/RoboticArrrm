package net.tangentmc;

public class NativeStuff
{
    public native void helloNative();

    public static void main(String[] args)
    {
        NarSystem.loadLibrary();
        new NativeStuff().helloNative();
    }
}