package ch.jalu.collectionbehavior.v2.documentation;

import ch.jalu.collectionbehavior.v2.method.CallEffect;

public class ListMethodBehavior {

    private final MethodDefinition methodDefinition;
    private final CallEffect effect;
    private final String result;

    public ListMethodBehavior(MethodDefinition methodDefinition, CallEffect effect, String result) {
        this.methodDefinition = methodDefinition;
        this.effect = effect;
        this.result = result;
    }

    @Override
    public String toString() {
        if (result != null) {
            return methodDefinition.methodName() + "(" + methodDefinition.arguments() + ") "
                + result + " " + effect;
        }
        return methodDefinition.methodName() + "(" + methodDefinition.arguments() + ") " + effect;
    }
}
