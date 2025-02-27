package dk.aau.cs.verification;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.verification.observations.Observation;

public class SMCSettings {

    public int timeBound;
    public int stepBound;
    public float falsePositives;
    public float falseNegatives;
    public float indifferenceWidth;
    public float confidence;
    public float estimationIntervalWidth;
    public boolean compareToFloat;
    public float geqThan;

    private List<Observation> observations;

    public static SMCSettings Default() {
        SMCSettings settings = new SMCSettings();
        settings.timeBound = 1000;
        settings.stepBound = Integer.MAX_VALUE;
        settings.falsePositives = 0.01f;
        settings.falseNegatives = 0.01f;
        settings.indifferenceWidth = 0.05f;
        settings.confidence = 0.95f;
        settings.estimationIntervalWidth = 0.05f;
        settings.compareToFloat = false;
        settings.geqThan = 0.5f;
        settings.setObservations(new ArrayList<>());
        return settings;
    }

    // Computes the number of runs needed according to :
    // https://link.springer.com/content/pdf/10.1007/b94790.pdf p.78-79
    // ONLY RELEVANT FOR PROBABILITY ESTIMATION !
    public int chernoffHoeffdingBound() {
        double bound = Math.log(2.0 / (1 - confidence)) / (2.0 * Math.pow(estimationIntervalWidth, 2));
        return (int) Math.ceil(bound);
    }

    // ONLY RELEVANT FOR PROBABILITY ESTIMATION !
    public float precisionFromRuns(int runsNeeded) {
        return (float) Math.sqrt(
            Math.log(2.0 / (1 - confidence)) / (2.0 * runsNeeded)
        );
    }

    public void setTimeBound(int timeBound) {
        this.timeBound = timeBound;
    }

    public void setStepBound(int stepBound) {
        this.stepBound = stepBound;
    }

    public int getTimeBound() {
        return timeBound;
    }

    public int getStepBound() {
        return stepBound;
    }

    public void setObservations(List<Observation> observations) {
        this.observations = observations;
    }

    public List<Observation> getObservations() {
        return observations;
    }
}
