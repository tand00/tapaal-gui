package net.tapaal.gui.petrinet.dialog;

import javax.swing.JSlider;

public class QuerySlider extends JSlider {
    private double realValue;
    private double desiredMin;
    private double desiredMax;

    public QuerySlider(int value, double desiredMin, double desiredMax) {
        super(0, 100, value);
        this.desiredMin = desiredMin;
        this.desiredMax = desiredMax;
    }

    public void setRealValue(double realValue) {
        this.realValue = realValue;
    }

    public double getRealValue() {
        return realValue;
    }

    public double getDesiredMin() {
        return desiredMin;
    }

    public double getDesiredMax() {
        return desiredMax;
    }
}