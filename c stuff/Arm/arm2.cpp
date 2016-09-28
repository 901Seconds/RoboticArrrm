/* testing arm*/

#include <stdio.h>
#include <math.h>

extern "C" int init(int);

// motor
extern "C" int Sleep(int s,int mu)

//camera
extern "C" int take_picture();
extern "C" int open_screen_stream();
extern "C" int close_screen_stream();
extern "C" int take_picture();
extern "C" int set_pixel(int x,int y,char r,char g,char b); 
extern "C" char get_pixel(int x,int y,char rgb); 

//function prototypes
void measure_angles(int);

typedef struct {
   int xm1;
   int ym1;
   int xm2;
   int ym2;
   int R;
   float theta1;
   float theta2;

} SCARA_ARM;

SCARA_ARM sarm;
// initialize the hardware
void init_arm()
{
   sarm.xm1 = 287;
   sarm.ym1 = 374;
   sarm.xm2 = 377;
   sarm.ym2 = 374;
   sarm.R = 154;
   // wait here
   Sleep(0,200000);
   sarm.theta1 = -90.0;
   sarm.theta2 = -90.0;
}

// measure angles of the elbows
void measure_angles()
{
   int phi = 0;
   int luma1[180];
   int luma2[180];
   //int i; 
   
   int x11,y11;
   int thr = 60;

   // go along the arc measuring luminicity
   int phi_count = 0;
   // for motor 1 - left, go clockwise from -180 to -90
   for (phi = -180; phi < -80; phi = phi+1)
   {
      phi_count = 180+phi;
      x11 = (int)(sarm.xm1+sarm.R*cos(phi*3.14152/180));
      y11 = (int)(sarm.ym1+sarm.R*sin(phi*3.14152/180));
      int lum= get_pixel(x11,y11,3);
      luma1[phi_count]= lum;
     if ( phi_count > 0) {
         if (luma1[phi_count]< luma1[phi_count-1] - thr){
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
                  set_pixel(xt,yt,255,0,0);
                } 
              if ((max_rad>min_rad +thr)&&(r_min<r_max)) n_votes++;               
            }
            if (n_votes >6) {sarm.theta1 = phi+7;};
       }
     }
   }

   phi_count = 0;
   for (phi = 0; phi > -100; phi = phi - 1)
   {
      phi_count = -phi;
      x11 = (int)(sarm.xm2+sarm.R*cos(phi*3.14152/180));
      y11 = (int)(sarm.ym2+sarm.R*sin(phi*3.14152/180));
      int lum= get_pixel(x11,y11,3);
      luma2[phi_count]= lum;
     if ( phi_count > 0) {
         if (luma2[phi_count]< luma2[phi_count-1] - thr){
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
                  set_pixel(xt,yt,255,0,0);
                } 
              if ((max_rad>min_rad +thr)&&(r_min<r_max)) n_votes++;               
            }
            if (n_votes >6) {sarm.theta2 = phi-7;};
       }
        
     }
   }
}

int main()
{
   init(0);
   init_arm();
   open_screen_stream();
   char input = getchar();
   while (input !='q')
   {
       if (input == 'm')
       {
          take_picture();
          measure_angles();
          printf("measured angles: theta1=%f theta2=%f\n",
               sarm.theta1, sarm.theta2);
       }
       input = getchar();
    }
   return 0;
}





