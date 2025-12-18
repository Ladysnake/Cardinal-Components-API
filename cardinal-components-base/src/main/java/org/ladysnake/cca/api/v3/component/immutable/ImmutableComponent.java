package org.ladysnake.cca.api.v3.component.immutable;

public interface ImmutableComponent {
    @FunctionalInterface
    interface Modifier<C extends ImmutableComponent, O> {
        C modify(C component, O attachedTo);
    }
    @FunctionalInterface
    interface Listener<C extends ImmutableComponent, O> extends Modifier<C, O> {
        void listen(C component, O attachedTo);
        @Override
        default C modify(C component, O attachedTo) {
            this.listen(component, attachedTo);
            return component;
        }
    }
}
