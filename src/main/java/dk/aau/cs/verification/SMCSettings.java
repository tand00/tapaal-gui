package dk.aau.cs.verification;

public class SMCSettings {

    public enum RunBoundType {

        TIMEBOUND, STEPBOUND;

        public String toString() {
            if(this == TIMEBOUND) {
                return "time";
            } else {
                return "steps";
            }
        }
    }

    public enum SMCSemantics {

        WEAK, STRONG;

        public String toString() {
            if(this == WEAK) {
                return "weak";
            } else {
                return "strong";
            }
        }
    }

    public RunBoundType boundType;
    public int boundValue;
    public float falsePositives;
    public float falseNegatives;
    public float indifferenceWidth;
    public float defaultRate;
    public float confidence;
    public float estimationIntervalWidth;
    public boolean compareToFloat;
    public float geqThan;
    public SMCSemantics semantics;

    public static SMCSettings Default() {
        SMCSettings settings = new SMCSettings();
        settings.boundType = RunBoundType.TIMEBOUND;
        settings.boundValue = 1000;
        settings.falsePositives = 0.01f;
        settings.falseNegatives = 0.01f;
        settings.indifferenceWidth = 0.05f;
        settings.defaultRate = 0.1f;
        settings.confidence = 0.95f;
        settings.estimationIntervalWidth = 0.05f;
        settings.compareToFloat = false;
        settings.geqThan = 0.5f;
        settings.semantics = SMCSemantics.WEAK;
        return settings;
    }

}
