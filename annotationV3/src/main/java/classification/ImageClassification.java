package classification;

import modelV3.Annotation;

public class ImageClassification extends Annotation {

    public ImageClassification() {
        this("");
    }

    public ImageClassification(String label) {
        this(label, "");
    }

    public ImageClassification(String label, String instance) {
        this(label, instance, false, false);
    }

    public ImageClassification(String label, String instance, boolean verified, boolean autoCreated) {
        super(label, instance, verified, autoCreated);
    }
}
