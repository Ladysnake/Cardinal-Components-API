package nerdhub.cardinal.components.internal;

import org.jetbrains.annotations.ApiStatus;

public abstract class StatefulLazy {
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

    protected void postInit() {
        // NO-OP
    }
}
