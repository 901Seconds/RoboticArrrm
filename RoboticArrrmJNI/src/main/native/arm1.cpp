/* testing arm*/

#include <stdio.h>
#include <time.h>
#include <math.h>

extern "C" int init(int);

// motor 
extern "C" int set_motor(int m, int s);
extern "C" int Sleep(int s,int mu);

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
int xm1 = 125;
int ym1 = 179;
int xm2 = 239;
int ym2 = 181;
// one arm length[pixels]
int R = 77;
// current angles of the elbows [deg]
int theta1,theta2;
int x_elbow_1,y_elbow_1;
int x_elbow_2,y_elbow_2;
// tool position
int x_tool_meas, y_tool_meas;

// current position of the pen
int x_pen,y_pen;

// set of segments to draw
typedef struct {
  int x1,y1;
  int x2,y2;
} Line;


// picture
Line lines_to_draw[1000];

// reads set of segments from text file
int read_draw_file()
{
   FILE *file;
   file = fopen("d1.txt","rw");
   if ( file == NULL)
   {  printf("Loser 0- check file name!!!!!\n");
      return -1;
   }
   char in_str[7]; 

   while (fscanf(file,"%s",in_str) != EOF) 
   {
     printf("in_str = %s   ", in_str);
     int x = 0;
     int y = 0;
     char pen;
     sscanf(in_str,"%d,%d,%c\n",&x,&y,&pen);
     printf("x=%d y=%d pen=%c\n",x,y,pen);
  
   }
   fclose(file);
   return 0; 
}

//
void motors_pulse(int u1,int u2,int dt)
{
   set_motor(1,u1);
   set_motor(2,u2);
   Sleep(0,dt*1000);
   set_motor(1,0);
   set_motor(2,0);
   Sleep(0,200000);
}

// takes measured upper arm angles and 
// calculates 2 possible positions of the tool
void calculate_tool_position()
{
   printf(" Calculate tool position\n");
   int xj1 = xm1 -R * cos (theta1*3.14152/180);
   int yj1 = ym1 -R * sin (theta1*3.14152/180);
   int xj2 = xm2 -R * cos (theta2*3.14152/180);
   int yj2 = ym2 -R * sin (theta2*3.14152/180);
   int dx = xj2 - xj1;
   int dy = yj2 - yj1;
   int d = sqrt(dx*dx + dy*dy);
   int l = d/2;
   int h =  sqrt(R*R - d*d/4);
   int xc1 = xj1 + l*dx/d + h*dy/d;
   int yc1 = yj1 + l*dy/d - h*dx/d;
   int xc2 = xj1 + l*dx/d - h*dy/d;
   int yc2 = yj1 + l*dy/d + h*dx/d;
   printf("   xc1=%d yc1=%d\n", xc1,yc1);
   printf("   xc2=%d yc2=%d\n", xc2,yc2);

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

int show_video(int count)
{
    while(count<1000){
     take_picture();
     measure_angles(1);
     update_screen();
     count++;
     printf("%d\n",count);
   }

}

int main()
{

   init(0);
   open_screen_stream();
    
   set_motor(1,0);
   set_motor(2,0);
  // int dt = 200000;
   //read_draw_file();

   
   // run 5 times
   int count = 0 ;   

   printf("Input?\n");
   char input = getchar();
   printf("%c\n",input);
   while(input !='q')
   {

     take_picture();
     measure_angles(1);
     calculate_tool_position();
     find_pen();
     update_screen();
//     if ( input == 'a') motors_pulse(0,140,30);
//     if ( input == 's') motors_pulse(0,-140,30);
//     if ( input == 'd') motors_pulse(140,0,30);
//     if ( input == 'f') motors_pulse(-140,0,30);
     if ( input == 'a') motors_pulse(0,240,8);
     if ( input == 's') motors_pulse(0,-240,8);
     if ( input == 'd') motors_pulse(240,0,8);
     if ( input == 'f') motors_pulse(-240,0,8);
     printf("Input?\n");
     input = getchar();
     printf("%c\n",input);
     // input ='n';
     //count++; 
    }
   return 0;
}





