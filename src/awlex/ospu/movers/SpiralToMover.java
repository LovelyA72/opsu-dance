package awlex.ospu.movers;

import itdelatrisu.opsu.Utils;
import itdelatrisu.opsu.objects.GameObject;
import yugecin.opsudance.movers.Mover;

/**
 * Created by Alex Wieser on 09.10.2016.
 * WHO DO YOU THINK I AM?
 *
 * This {@link Mover} ends the spiral from the start object
 */
public class SpiralToMover extends Mover {

    /**
     * How many times the cursor goes around the center
     * For satisfying results this musn't be a multiple of 2
     */
    private final int CIRCLENAVIGATIONS = 1;

    double startAng;
    double startRad;

    public SpiralToMover(GameObject start, GameObject end, int dir) {
        super(start, end, dir);
        startAng = Math.atan2(endY - startY, endX - startX) + Math.PI;
        startRad = Utils.distance(startX, startY, endX, endY);
        if (startRad < 20)
            startRad = 20 + startRad / 20d;
    }

    @Override
    public double[] getPointAt(int time) {
        double rad = startRad * (1 - getT(time));
        double ang = CIRCLENAVIGATIONS * (startAng + 2d * Math.PI * (1 - getT(time)) * dir);
        return new double[]{
                endX + rad * Math.cos(ang),
                endY + rad * Math.sin(ang)
        };
    }

    @Override
    public String getName() {
        return "Spiral2";
    }
}
