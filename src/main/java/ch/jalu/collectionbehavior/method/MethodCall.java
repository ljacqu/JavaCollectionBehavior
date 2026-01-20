package ch.jalu.collectionbehavior.method;

import java.util.Set;

public interface MethodCall<C> {

    void invoke(C container);

    Set<MethodCallProperty> properties();

}
