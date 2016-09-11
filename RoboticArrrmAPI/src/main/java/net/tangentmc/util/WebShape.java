package net.tangentmc.util;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class WebShape {
    public double[] xpoints;
    public double[] ypoints;
    boolean penDown;
}
