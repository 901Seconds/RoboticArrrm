/* testing arm*/

#include <stdio.h>
#include <time.h>
#include <math.h>
#include <net_tangentmc_RoboticArmJNI.h>

extern "C" int init(int);

// motor 
extern "C" int set_motor(int m, int s);
extern "C" int Sleep(int s,int mu);
extern "C" int set_PWM(int chan, int value);
extern "C" int set_PWM_frequency(int chan, int freq);
extern "C" int set_servo(int chan, int value);

//camera
extern "C" int take_picture();
extern "C" int open_screen_stream();
extern "C" int close_screen_stream();
extern "C" int take_picture();
extern "C" int update_screen();
extern "C" int save_picture(char fn[5]);
extern "C" int set_pixel(int x,int y,char r,char g,char b); 
extern "C" char get_pixel(int x,int y,char rgb); 


// coordinates of the motors[pixels]
int xm1 = 80;
int ym1 = 164;
int xm2 = 129;
int ym2 = 165;
// one arm length[pixels]
int R = 60;
// current angles of the elbows [deg]
int theta1,theta2;
int x_elbow_1,y_elbow_1;
int x_elbow_2,y_elbow_2;
// tool position
int x_tool_meas, y_tool_meas;

// current position of the pen
int x_pen,y_pen;

// 2D Point type
typedef struct {
   int x,y;           // cartesian coordinates
   int theta1,theta2; // angles of the motors
   char pen;
} MoveTo;


// set of segments to draw
typedef struct {
  int x1,y1;
  int x2,y2;
} Line;

void init()
{
   theta1 = 90;
   theta2 = 90;
   float pwm1 = 1500 + (theta1-90)*(1000.0/180.0);
   float pwm2 = 1500 + (theta2-90)*(1000.0/180.0);
   set_servo(0, (int)pwm1);
   set_servo(1, (int)pwm2);
   // wait here 
    printf(" init:  pwm1=%d pwm2=%d\n",(int)pwm1,(int)pwm2);    
   Sleep(2,0);
}


// sets angles of main servos
void set_angles(int in1,int in2)
{
   float pwm1_s = 1500 + (theta1-90)*(1000.0/180.0);
   float pwm2_s = 1500 + (theta2-90)*(1000.0/180.0);
   float pwm1_f = 1500 + (in1-90)*(1000.0/180.0);
   float pwm2_f = 1500 + (in2-90)*(1000.0/180.0);

   printf(" current: theta1=%d theta2=%d\n",theta1,theta2);
   printf(" goal  : in1=%d in2=%d\n",in1,in2);
  
   printf("   start : 1=%f 2=%f\n", pwm1_s, pwm2_s);
   printf("   finis : 1=%f 2=%f\n", pwm1_f, pwm2_f);

   float pwm1 = pwm1_s;
   float pwm2 = pwm2_s;
   int n;
   int N = 50;
   for ( n = 0; n < N; n++)
   {  
      pwm1 = pwm1+  (pwm1_f-pwm1_s)/((float)N);
      pwm2 = pwm2 + (pwm2_f-pwm2_s)/((float)N);
      set_servo(0, (int)pwm1);
      set_servo(1, (int)pwm2); 
      // wait here 
      //printf(" n=%d pwm1=%d pwm2=%d\n",n,(int)pwm1,(int)pwm2);  
      take_picture();
      update_screen();  
      Sleep(0,10000);
   }
   theta1 = in1;
   theta2 = in2;

}

JNIEXPORT void JNICALL Java_net_tangentmc_RoboticArmJNI_setServo(JNIEnv *, jobject, jint pin, jint pulseWidth) {
    int pinNum = (int)(pin);
    int pulseWidthMS = (int)(pulseWidth);
    set_servo(pinNum,pulseWidthMS);
}

JNIEXPORT void JNICALL Java_net_tangentmc_RoboticArmJNI_init(JNIEnv *, jobject) {
 init();
}

// drops/raises pen: 0 - up, 1-down
void pen_up_down(int dir)
{
   if (dir == 1)
   {
     set_servo(2, 1000);
   }
   if (dir == 0)
   {
     set_servo(2, 2000);
   }
   Sleep(1,0);

}



// measure angles of the elbows
void measure_angles(int deb)
{
   int phi = 0;
   char luma1[180];
   char luma2[180];
   //int histogramm[255];
   long int aver = 0;
   
   int x11,y11,x22,y22;
   //int r1,g1,b1,r2,g2,b2,l1,l2;
   char min = 255;
   char max = 0;
   // go along 
   for (phi = 0;phi<180;phi++)
   {
      x11 = (int)(xm1-R*cos(phi*3.14152/180));
      y11 = (int)(ym1-R*sin(phi*3.14152/180));
      x22 = (int)(xm2-R*cos(phi*3.14152/180));
      y22 = (int)(ym2-R*sin(phi*3.14152/180));
      luma1[phi]= get_pixel(x11,y11,3);
      luma2[phi]= get_pixel(x22,y22,3);

      if ( luma1[phi] > max) { max = luma1[phi]; }
      if ( luma2[phi] > max) { max = luma2[phi]; }
      if ( luma1[phi] < min) { min = luma1[phi]; }        
      if ( luma2[phi] < min) { min = luma2[phi]; }
     
      aver = aver + luma1[phi];
      set_pixel(x11,y11,0,255,0);
      set_pixel(x22,y22,0,255,0);
 
   }

   if (deb) {
   //  for ( phi = 0; phi<180;phi++)
   //   {
   //     printf(" %d %d %d\n", phi, luma1[phi], luma2[phi]);
   //   }
    }
    aver = aver/180;
    //printf("min=%d max=%d aver=%d\n", min, max,aver);
    // convolution
    int conv1[180];    
    int conv2[180];    
    for (phi =  0; phi<180;phi++)
    {
      conv1[phi] = 0;
      conv2[phi] = 0;
    } 
    int W = 20;
    int i; 
    //int offset = 0;
    int conv1_max = 0;
    int phi1_max = -1;
    int conv2_max = 0;
    int phi2_max = -1;

    for (phi =  W/2; phi < 180 - W/2;phi++)
    {  
       for (i=phi-W/2;i<phi+W/2;i++)
       {
          conv1[phi]=conv1[phi] + (255 - luma1[i]);
          conv2[phi]=conv2[phi] + (255 - luma2[i]);
       }
       // find maximum
       if ( conv1[phi] > conv1_max) {
          conv1_max = conv1[phi];
          phi1_max = phi;
       }
       if ( conv2[phi] > conv2_max) {
          conv2_max = conv2[phi];
          phi2_max = phi;
       }
    }
   // for (phi = 0; phi < 180;phi++)
   // {  
   //   printf("%d %d %d\n", phi,conv1[phi],conv2[phi]);
   // }
    theta1 = phi1_max;
    theta2 = phi2_max;
    printf("measured angles: theta1=%d theta2=%d\n", theta1, theta2);

    // calculate elbow positions
    x_elbow_1 = xm1 - R*cos(theta1*3.14129/180.0);
    y_elbow_1 = ym1 - R*sin(theta1*3.14129/180.0);
    x_elbow_2 = xm2 - R*cos(theta2*3.14129/180.0);
    y_elbow_2 = ym2 - R*sin(theta2*3.14129/180.0);
    //draw elbows as red rectangles
    for (x11=x_elbow_1-3;x11<x_elbow_1+3;x11++)
     for (y11=y_elbow_1-3;y11<y_elbow_1+3;y11++)
       set_pixel(x11,y11,255,0,0);

     for (x22=x_elbow_2-3;x22<x_elbow_2+3;x22++)
     for (y22=y_elbow_2-3;y22<y_elbow_2+3;y22++)
       set_pixel(x22,y22,255,0,0); 
   //Sleep(5,0);
}



// for known elbow positions
int find_pen()
{
   int phi = 0;
   char luma[360];
   //int histogramm[255];
   long int aver = 0;
   
   int x,y;
   //int r1,g1,b1,r2,g2,b2,l1,l2;
   char min = 255;
   char max = 0;
   for (phi = 0;phi<360;phi++)
    {
      x = (int)(x_elbow_2 - R*cos(phi*3.14152/180));
      y = (int)(y_elbow_2 - R*sin(phi*3.14152/180));
      //luma[phi]= get_pixel(x,y,3);
      set_pixel(x,y,0,255,0);
    }
   // go along 
   for (phi = 0;phi<360;phi++)
    {
      x = (int)(x_elbow_1 - R*cos(phi*3.14152/180));
      y = (int)(y_elbow_1 - R*sin(phi*3.14152/180));
      luma[phi]= get_pixel(x,y,3);
       set_pixel(x,y,0,255,0);
    }
     
    //for ( phi = 0; phi<360;phi++)
   // {
   //   printf(" %d %d\n", phi, luma[phi]);
   // }
    int conv[360];    
    for (phi =  0; phi < 360;phi++)
    {
      conv[phi] = 0;
    } 
    int W = 20;
    int i; 
    int conv_max = 0;
    int phi_max = 0;
    for (phi =  W/2; phi < 360 - W/2 ; phi++)
    {  
       for (i=phi-W/2;i<phi+W/2;i++)
       {
          conv[phi]=conv[phi] + (255 - luma[i]);
       }
       // keep track of maximum
       if ( conv[phi] > conv_max) {
          conv_max = conv[phi];
          phi_max = phi;
       }
    }
    printf("Tool tracking: phi_max(to left joint)=%d\n",phi_max);
    x_tool_meas = x_elbow_1 - R*cos(phi_max*3.14129/180.0);
    y_tool_meas = y_elbow_1 - R*sin(phi_max*3.14129/180.0);
    printf(" x_tool_meas=%d y_tool_meas=%d\n", x_tool_meas, y_tool_meas);

    for (x= x_tool_meas-3; x<x_tool_meas+3 ; x++)
     for (y = y_tool_meas - 3 ; y < y_tool_meas+3 ; y++)
       set_pixel(x,y,255,0,0); 
}





