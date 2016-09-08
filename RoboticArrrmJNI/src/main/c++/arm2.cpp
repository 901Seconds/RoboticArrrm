/* testing arm*/

#include <stdio.h>
#include <time.h>
#include <sys/time.h>
#include <math.h>
#include "arm2.h"

extern "C" int init(int);

// motor 
extern "C" int set_motor(int m, int s);
extern "C" int Sleep(int s,int mu);
extern "C" int set_PWM(int chan, int value);
extern "C" int set_PWM_frequency(int chan, int freq);
extern "C" int set_servo(int chan, int value);
extern "C" int select_IO(int chan, int direct);
extern "C" int write_digital(int chan,char level);

//camera
extern "C" int take_picture();
extern "C" int open_screen_stream();
extern "C" int close_screen_stream();
extern "C" int take_picture();
extern "C" int update_screen();
extern "C" int save_picture(char fn[5]);
extern "C" int set_pixel(int x,int y,char r,char g,char b); 
extern "C" char get_pixel(int x,int y,char rgb);

typedef struct {
   int xm1;
   int ym1;
   int xm2;
   int ym2;
   int R;
   float theta1;
   float theta2;
   int xj1;
   int yj1;
   int xj2;
   int yj2;
   int xpen;
   int ypen;
   struct timeval time1;
   struct timeval time2;

} SCARA_ARM;

SCARA_ARM sarm;


// 2D Point type
typedef struct {
   int x,y;           // cartesian coordinates
   float theta1,theta2; // angles of the motors
   char pen;
   int pwm1,pwm2,pwm3;
} ArmState;


// picture to draw
typedef struct {
  int n_seg;
  ArmState states[1000]; // ugly, is it not?
} Job;

Job job;
int STEP = 0;

FILE *debug_file;

// initialize the hardware
void init_arm()
{
   
   printf(" init()\n");    
   sarm.xm1 = 287;
   sarm.ym1 = 374;
   sarm.xm2 = 377;
   sarm.ym2 = 374;
   sarm.R = 154;
  
   printf(" init()\n");  
   select_IO(0,0);
   select_IO(1,0);
   select_IO(2,0);
   write_digital(0,0);
   write_digital(1,0);
   write_digital(2,0);
   // wait here 
   
   Sleep(0,200000);
   gettimeofday(&sarm.time1,NULL);
   debug_file = fopen("debug.txt","w+");
   if (!debug_file) {
     printf("Can not open debug file\n");
     return;
   }
   sarm.theta1 = -90.0;
   sarm.theta2 = -90.0;

   //set_angles(sarm.theta1,sarm.theta2);
   //pen_up_down(0);
   printf(" init()\n");  
}

void print_arm_data()
{
   printf(" Motor positions: xm1=%d ym1=%d xm2=%d ym2=%d\n",
             sarm.xm1,sarm.ym1,sarm.xm2,sarm.ym2);
   printf(" Joint positions: xj1=%d yj1=%d xj2=%d yj2=%d\n",
             sarm.xj1,sarm.yj1,sarm.xj2,sarm.yj2);
}

// prints picture data
void print_picture_data()
{
   int i;
   printf(" Picture data: %d segments\n",job.n_seg); 
   for (i = 0 ; i < job.n_seg ; i++)
   {
     printf(" i=%d  pwm1=%d pwm2=%d pwm3=%d\n",
       i,job.states[i].pwm1, job.states[i].pwm2, job.states[i].pwm3);
   } 
}


// reads set of segments from text file
int read_draw_file(const char* file_name)
{
   FILE *file;
   file = fopen(file_name,"rw");
   if ( file == NULL)
   {  printf("Loser 0- check file name!!!!!\n");
      return -1;
   }
   char in_str[64]; 
   int p1 = 0.0;
   int p2 = 0.0;
   int p3 = 0;
   job.n_seg = 0;
   while (fscanf(file,"%s",in_str) != EOF) 
   {
     printf("in_str = %s   ", in_str);
     sscanf(in_str,"%d,%d,%d", &p1,&p2,&p3);
     printf("p1=%d p2=%d p3=%d\n",p1,p2,p3);

     job.states[job.n_seg].pwm1 = p1; 
     job.states[job.n_seg].pwm2 = p2; 
     job.states[job.n_seg].pwm3 = p3; 
     printf("   p1=%d p2=%d p3=%d\n",
       job.states[job.n_seg].pwm1,job.states[job.n_seg].pwm2,
       job.states[job.n_seg].pwm3);
     job.n_seg++;
   }
   fclose(file);
   return 0; 
}


// sets servo motor angles
// because of the conventions angles are negative
// pwm values are positive
void set_motors(int in1,int in2, int in3)
{
 // printf("    set_angle(): t1=%f t2=%f\n",in1, in2);
 // gettimeofday(&sarm.time1,NULL);
  if( (in1<1000) || (in1>2000)){
     printf(" pwm1 out of range\n");
     return;
  }
  if( (in2<1000) || (in2>2000)){
     printf(" pwm1 out of range\n");
     return;
  }
  if( (in3<1000) || (in3>2000)){
     printf(" pwm1 out of range\n");
     return;
  }
  set_servo(0, in1);
  set_servo(1, in2); 
  set_servo(2, in3); 

   take_picture();
   measure_angles(0);
   gettimeofday(&sarm.time2,NULL);
   double dt = (double)(sarm.time2.tv_usec -sarm.time1.tv_usec)/1000.0+
              (double)(sarm.time2.tv_sec -sarm.time1.tv_sec)*1000.0;
   fprintf(debug_file,"%f %f %f\n", dt,sarm.theta1,sarm.theta2);
}


// draw whole thing
void make_job()
{
   int i;
   //printf(" Picture data: %d segments\n",pic.n_seg); 
   //printf(" Motor positions: xm1=%d ym1=%d xm2=%d ym2=%d\n",
   //         xm1,ym1,xm2,ym2);
   for (i = 0 ; i < job.n_seg ; i++)
   {
     printf("seg = %d p1=%d p2=%d p3=%d\n",
       i,job.states[i].pwm1, job.states[i].pwm2, job.states[i].pwm3);
     printf("  move now..\n");
     // pic.lines[i].pen == 'd' - 1(true) or 0 
//     pen_up_down((int)( pic.lines[i].pen));
     set_motors(job.states[i].pwm1, job.states[i].pwm2, job.states[i].pwm3);
     take_picture();
     measure_angles(0);
     draw_motors();
     update_screen();
     if (STEP) {
       printf(" Push ENTER\n");
       char stop = getchar();
     }
     //Sleep(1,0);
   } 
}


// draw dots and circles at video output
void draw_motors()
{
  int phi;
  int x11,y11,x22,y22;
  for (x11=sarm.xm1-3; x11 < sarm.xm1+3 ; x11++)
  for (y11=sarm.ym1-3; y11 < sarm.ym1+3;  y11++)
       set_pixel(x11,y11,255,0,0); 

  for (x22=sarm.xm2-3; x22 < sarm.xm2+3 ; x22++)
  for (y22=sarm.ym2-3; y22 < sarm.ym2+3 ; y22++)
       set_pixel(x22,y22,255,0,0); 

  for (phi = -180;phi < -80; phi++)
   {
      x11 = (int)(sarm.xm1+sarm.R*cos(phi*3.14152/180));
      y11 = (int)(sarm.ym1+sarm.R*sin(phi*3.14152/180));
      set_pixel(x11,y11,0,255,0);
   }
   for (phi = 0;phi > -100; phi--)
   {
      x22 = (int)(sarm.xm2+sarm.R*cos(phi*3.14152/180));
      y22 = (int)(sarm.ym2+sarm.R*sin(phi*3.14152/180));
      set_pixel(x22,y22,0,255,0);
   }
}

void draw_joints()
{
   //draw elbows as red rectangles
    int x11,y11,x22,y22;
    for (x11=sarm.xj1-3;x11<sarm.xj1+3;x11++)
     for (y11=sarm.yj1-3;y11<sarm.yj1+3;y11++)
       set_pixel(x11,y11,255,0,0);

     for (x22=sarm.xj2-3;x22<sarm.xj2+3;x22++)
     for (y22=sarm.yj2-3;y22<sarm.yj2+3;y22++)
       set_pixel(x22,y22,255,0,0); 
}
double measure_angle(int motor) {
    measure_angles(0);
    if (motor == 1)
        return sarm.theta1;
    else
        return sarm.theta2;
}
// measure angles of the elbows
void measure_angles(int deb)
{
   int phi = 0;
   int luma1[180];
   int luma2[180];
   //int i; 
   
   int x11,y11;
   int thr = 60;

   FILE *luma;
   if (deb==2) {
     printf("Opening file for debugging\n");
     luma = fopen("luma.txt", "w=");
   }


   // go along the arc measuring luminicity
   int phi_count = 0;
   // for motor 1 - left, go clockwise from -180 to -90
   for (phi = -180; phi < -80; phi = phi +1)
   { 
   
      phi_count = 180+phi;
      x11 = (int)(sarm.xm1+sarm.R*cos(phi*3.14152/180));
      y11 = (int)(sarm.ym1+sarm.R*sin(phi*3.14152/180));
      //x22 = (int)(sarm.xm2+sarm.R*cos(phi*3.14152/180));
      //y22 = (int)(sarm.ym2+sarm.R*sin(phi*3.14152/180));
      int lum= get_pixel(x11,y11,3);
      luma1[phi_count]= lum;
      if (deb==2) {
          printf("phi1 = %d x11=%d y11=%d phic=%d lum=%d\n", 
            phi, x11,y11,phi_count, luma1[phi_count]);      
      }

     if ( phi_count > 0) {
         if (luma1[phi_count]< luma1[phi_count-1] - thr){
            // going down - may be circle
            printf("    EDGE: x=%d y=%d\n", x11,y11);
            //calculate possible centre of the circle
            int xc = (int)(sarm.xm1+sarm.R*cos((phi+15/2)*3.14152/180));
            int yc = (int)(sarm.ym1+sarm.R*sin((phi+15/2)*3.14152/180));
      
            int j;
            int circle_edges[8]; // try 8 normals of the circle
            int n_votes=0;
            for (j = 0 ; j < 8; j++){
                circle_edges[j] = 0 ; 
                int rt;
                int max_rad = 0;
                int min_rad = 255;
                int r_min = 0;
                int r_max = 0;
                for (rt = 15; rt < 30; rt = rt + 1){
                  int xt = xc + rt*cos(j*45*3.14152/180);
                  int yt = yc + rt*sin(j*45*3.14152/180);
                  int lt0 = get_pixel(xt,yt,3);
                  if (lt0 > max_rad){
                    max_rad = lt0;
                    r_max = rt;
                  }
                  if (lt0 < min_rad){
                    min_rad = lt0;
                    r_min = rt;  
                  }
                 // printf("       j=%d  lt0 = %d max=%d min=%d\n",
                 //               j,lt0,max_rad,min_rad);
                  set_pixel(xt,yt,255,0,0);
                } 
              if ((max_rad>min_rad +thr)&&(r_min<r_max)) n_votes++;               
            } 
            printf("    n_votes=%d\n",n_votes);
            if (n_votes >6) {sarm.theta1 = phi+7;};
       } // luma>luma
        
     } //phi
   } //for phi

 // go along the arc measuring luminicity
   phi_count = 0;
   // for motor 2- right, go anti clockwise from 0 to -100
   for (phi = 0; phi > -100; phi = phi - 1)
   { 
   
      phi_count = -phi;
      x11 = (int)(sarm.xm2+sarm.R*cos(phi*3.14152/180));
      y11 = (int)(sarm.ym2+sarm.R*sin(phi*3.14152/180));
      int lum= get_pixel(x11,y11,3);
      luma2[phi_count]= lum;
      if (deb==2) {
          printf("phi2 = %d x11=%d y11=%d phic=%d lum=%d\n", 
             phi, x11,y11,phi_count, luma2[phi_count]);      
     }
     if ( phi_count > 0) {
         if (luma2[phi_count]< luma2[phi_count-1] - thr){
            // going down - may be circle
            printf("    EDGE: x=%d y=%d\n", x11,y11);
            //calculate possible centre of the circle
            int xc = (int)(sarm.xm2+sarm.R*cos((phi-15/2)*3.14152/180));
            int yc = (int)(sarm.ym2+sarm.R*sin((phi-15/2)*3.14152/180));
      
            int j;
            int circle_edges[8]; // try 8 normals of the circle
            int n_votes=0;
            for (j = 0 ; j < 8; j++){
                circle_edges[j] = 0 ; 
                int rt;
                int max_rad = 0;
                int min_rad = 255;
                int r_min = 0;
                int r_max = 0;
                for (rt = 15; rt < 30; rt = rt + 1){
                  int xt = xc + rt*cos(j*45*3.14152/180);
                  int yt = yc + rt*sin(j*45*3.14152/180);
                  int lt0 = get_pixel(xt,yt,3);
                  if (lt0 > max_rad){
                    max_rad = lt0;
                    r_max = rt;
                  }
                  if (lt0 < min_rad){
                    min_rad = lt0;
                    r_min = rt;  
                  }
                 // printf("       j=%d  lt0 = %d max=%d min=%d\n",
                 //               j,lt0,max_rad,min_rad);
                  set_pixel(xt,yt,255,0,0);
                } 
              if ((max_rad>min_rad +thr)&&(r_min<r_max)) n_votes++;               
            } 
            printf("    n_votes=%d\n",n_votes);
            if (n_votes >6) {sarm.theta2 = phi-7;};
       } // luma>luma
        
     } //phi
   } //for phi

    // calculate elbow positions
    sarm.xj1 = sarm.xm1 + sarm.R*cos(sarm.theta1*3.14129/180.0);
    sarm.yj1 = sarm.ym1 + sarm.R*sin(sarm.theta1*3.14129/180.0);
    sarm.xj2 = sarm.xm2 + sarm.R*cos(sarm.theta2*3.14129/180.0);
    sarm.yj2 = sarm.ym2 + sarm.R*sin(sarm.theta2*3.14129/180.0);
    draw_joints();

     printf("measured angles: theta1=%f theta2=%f\n", 
              sarm.theta1, sarm.theta2);
    if (deb == 2){
      fclose(luma);
    }
 
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
   return 0;
}

// edit arm parameters
void set_arm(){
    printf("xm1=?\n");
    scanf("%d",&(sarm.xm1));
    printf("ym1=?\n");
    scanf("%d",&(sarm.ym1));
    printf("xm2=?\n");
    scanf("%d",&(sarm.xm2));
    printf("ym2=?\n");
    scanf("%d",&(sarm.ym2));
    printf("R=?\n");
    scanf("%d",&(sarm.R));

}

int main()
{
   init(0);  // initialize GPIO
   init_arm();
   open_screen_stream();
   
   printf("Input:\n s-set motors \n m-measure angles \n f-read file \n e-execute job \n p-take and save picture \n i-print arm configuration \n o-edit arm parameters \n q-out?\n");
   char input = getchar();
   printf("%c\n",input);

   while (input !='q')
   {   printf("Input: \n s-set motors \n m-measure angles \n f-read file \n e-execute job \n p-take and save picture \n i-print arm configuration \n o-edit arm parameters \n q-out?\n");
      printf("input = %c\n",input); 
    
      if (input =='s') // set servos pwm
       {
         printf("pwm1?\n");
         int p1;
         scanf("%d",&p1);
         printf("pwm2?\n");
         int p2;
         scanf("%d",&p2);
         int p3;
         printf("pwm3?\n");
         scanf("%d",&p3);
         printf("p1 = %d p2=%d p3=%d\n",p1,p2,p3);
         set_motors(p1,p2,p3);
       }

       if (input =='p') // take and save picture
       {
          take_picture();
          update_screen();
          save_picture("arm100");
       }
       if (input == 'e') // ENTIRE thing - draw
       { // draw whole file
         make_job();
       } 
       if (input == 'm')
       { // measure arm angles
          printf("measure arm angles\n");
          take_picture();
          measure_angles(2);
          draw_motors();
          update_screen();
       } 

      if (input == 'f') // load file
       { // open file
          char fn[256];
          printf(" Enter file name\n");
          scanf("%s",&(fn[0]));  
          read_draw_file(fn);
          //convert_xy_to_theta();
          print_picture_data();
       }

       if (input == 'i') // print arm info
       {
           printf("xm1=%d ym1=%d xm2=%d ym2=%d R=%d \n",
             sarm.xm1, sarm.ym1,sarm.xm2,sarm.ym2,sarm.R);
       }
       if (input == 'o') //edit arm parameters
       {
          set_arm();
       }
       printf("input ?\n");
       input = getchar();
       //printf("%c\n",input);
    }
    fclose(debug_file);
   return 0;
}





