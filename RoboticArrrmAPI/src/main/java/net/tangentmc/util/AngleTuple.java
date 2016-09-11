package net.tangentmc.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.awt.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class AngleTuple {
    double theta1,theta2;
    boolean penDown;
}