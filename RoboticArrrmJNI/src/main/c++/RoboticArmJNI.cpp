#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include "arm2.h"
extern "C" {
JNIEXPORT void JNICALL Java_net_tangentmc_RoboticArmJNI_setServo
  (JNIEnv * env, jobject obj, jint servo, jdouble pulse)
{
    set_servo((int)servo,(double)pulse);
}
JNIEXPORT void JNICALL Java_net_tangentmc_RoboticArmJNI_init
  (JNIEnv * env, jobject obj)
{
    init_bot();
}
JNIEXPORT jdouble JNICALL Java_net_tangentmc_RoboticArmJNI_readAngle
  (JNIEnv * env, jobject obj, jint arm)
{
    return measure_angle(arm);
}
}