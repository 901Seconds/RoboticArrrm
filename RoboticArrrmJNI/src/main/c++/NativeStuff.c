#include <stdio.h>
#include <stdlib.h>
#include <jni.h>

JNIEXPORT void JNICALL Java_net_tangentmc_NativeStuff_helloNative
  (JNIEnv * env, jobject obj)
{
    puts("Hello from C!");
}