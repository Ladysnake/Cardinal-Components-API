package nerdhub.cardinal.components.internal;

import org.jetbrains.annotations.ApiStatus;

public abstract class DispatchingLazy {
    private boolean requiresInitialization = true;
    private boolean loading = false;

    public void ensureInitialized() {
        if (this.requiresInitialization) {
            if (this.loading) {
                this.onCircularLoading();
            } else {
                this.loading = true;

                try {
                    this.init();
                } finally {
                    this.loading = false;
                }

                this.requiresInitialization = false;
                this.postInit();
            }
        }
    }

    protected void onCircularLoading() {
        throw new IllegalStateException("Circular loading issue, a mod is probably registering a ComponentType in the wrong place");
    }

    @ApiStatus.OverrideOnly
    protected abstract void init();

    protected void checkLoading(Class<?> ownerClass, String methodName) {
        if (!this.loading) {
            throw new IllegalStateException(ownerClass.getSimpleName() + "#" + methodName + " called at the wrong time");
        }
    }

    protected void postInit() {
        // NO-OP
    }
}
