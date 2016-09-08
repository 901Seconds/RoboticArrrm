

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
void measure_angles(int);
void draw_motors();
void pen_up_down(int dir);
void set_angles(float in1,float in2);
double measure_angle(int motor);
