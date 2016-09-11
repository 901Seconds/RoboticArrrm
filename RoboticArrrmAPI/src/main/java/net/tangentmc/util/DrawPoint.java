package net.tangentmc.util;

import lombok.*;

import java.awt.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DrawPoint {
    double x,y;
    boolean penDown;

    public void scale(double scale) {
        x*=scale;
        y*=scale;
    }
    public double dist(DrawPoint other) {
        if (other == null) return 0;
        return Point.distance(x,y,other.x,other.y);
    }

    public DrawPoint cpy() {
        return new DrawPoint(x,y,penDown);
    }
}
