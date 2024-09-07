package org.demo.wpplugin.geometry;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class Smoother {
    final HashMap<Point, Float> xSmoothedPoints;
    final int radius;
    final HeightDimension dimension;
    final HashSet<Point> points;
    final Collection<Point> corePoints;
    public Smoother(Collection<Point> points, int radius, HeightDimension dimension) {
        this.radius = radius;
        this.xSmoothedPoints = new HashMap<>(points.size());
        this.dimension = dimension;
        this.points = new HashSet<>(points);
        this.corePoints = points;
        for (Point point : points) {
            for (int y = -radius; y <= radius; y++) {
                this.points.add(new Point(point.x, point.y + y));
            }
        }
    }

    public void smoothAverage() {
        float[] kernel = new float[2 * radius + 1];
        float sum = kernel.length;
        Arrays.fill(kernel, 1);

        smoothPoints(kernel, sum);
    }

    /**
     * Generates a Gaussian curve as a float array.
     *
     * @param size   Number of points in the curve
     * @param maxHeight Maximum height of the curve (A in the Gaussian function)
     * @param mean   The mean (center) of the curve (μ in the Gaussian function)
     * @param stdDev The standard deviation (width) of the curve (σ in the Gaussian function)
     * @return A float array representing the Gaussian curve
     */
    public static float[] generateGaussianCurve(int size, float maxHeight, float mean, float stdDev) {
        float[] curve = new float[size];

        // Generate the Gaussian curve
        for (int i = 0; i < size; i++) {
            // Calculate the x value for each point
            float x = i;

            // Apply the Gaussian formula
            float value = (float) (maxHeight * Math.exp(-Math.pow(x - mean, 2) / (2 * Math.pow(stdDev, 2))));

            // Store the value in the curve array
            curve[i] = value;
        }

        return curve;
    }

    public void smoothGauss() {
        float[] kernel = generateGaussianCurve(2*radius+1,10,radius,radius/2f);
        float sum = 0;
        for (float v : kernel) {
            System.out.println("#".repeat((int)v));
            sum += v;
        }
        System.out.println(Arrays.toString(kernel));
        smoothPoints(kernel, sum);
    }

    public void smoothPoints(float[] kernel, float kernelSum) {
        //smooth in x dir, store locally to not influence other points being calculated
        for (Point curvePoint : points) {
            float sum = 0;
            for (int x = 0; x < kernel.length; x++) {
                float factor = kernel[x];
                int xPos = curvePoint.x + x - radius;
                int yPos = curvePoint.y;
                sum += dimension.getHeight(xPos, yPos) * factor;
            }
            sum /= kernelSum;
            xSmoothedPoints.put(curvePoint, sum);
        }

        //smooth in y dir using precalculated smoothed x values
        for (Point curvePoint : corePoints) {
            float sum = 0;
            for (int y = 0; y < kernel.length; y++) {
                float factor = kernel[y];
                sum += xSmoothedPoints.get(new Point(curvePoint.x, curvePoint.y + y - radius))* factor;
            }
            sum /= kernelSum;
            dimension.setHeight(curvePoint.x, curvePoint.y, sum);
        }
    }
}
