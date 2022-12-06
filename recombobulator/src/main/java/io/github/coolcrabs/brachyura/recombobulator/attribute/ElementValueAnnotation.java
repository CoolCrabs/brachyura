package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class ElementValueAnnotation extends ElementValue {
    public Annotation annotation;

    public ElementValueAnnotation(Annotation annotation) {
        super(ElementTag.ANNOTATION_INTERFACE);
        this.annotation = annotation;
    }

    @Override
    int byteSize() {
        return super.byteSize() + annotation.byteSize();
    }

    @Override
    void write(RecombobulatorOutput o) {
        super.write(o);
        annotation.write(o);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + tag.value;
        result = 37*result + annotation.hashCode();
        return result;
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitElementValueAnnotation(this);
        annotation.accept(v);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ElementValueAnnotation) {
            ElementValueAnnotation o = (ElementValueAnnotation) obj;
            if (
                annotation.equals(o.annotation) &&
                tag == o.tag
            ) return true;
        }
        return false;
    }
}
