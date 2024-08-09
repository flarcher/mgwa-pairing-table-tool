package org.mgwa.w40k.pairing.api.service;

import org.mgwa.w40k.pairing.api.model.AppStatus;
import org.mgwa.w40k.pairing.util.LoggerSupplier;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Handle the API REST lifecycle.
 */
public class StatusService {

    public StatusService(Runnable onExit) {
        this.onExit = Objects.requireNonNull(onExit);
        this.dryExit = Optional.ofNullable(System.getenv("API_SERVER_DEBUG"))
                .map(envValue -> !envValue.equalsIgnoreCase("false"))
                .orElse(false);
    }

    private final Runnable onExit;
    private final boolean dryExit;
    private final AtomicReference<AppStatus> currentStatus = new AtomicReference<>(AppStatus.INITIALIZING);

    public AppStatus getStatus() {
        return currentStatus.get();
    }

    public boolean initialized() {
        return currentStatus.compareAndSet(AppStatus.INITIALIZING, AppStatus.RUNNING);
    }

    public boolean exiting() {
        AppStatus oldStatus = currentStatus.getAndUpdate( status -> {
            if (status != AppStatus.EXITING && !dryExit) {
                onExit.run();
            }
            if (dryExit) {
                LoggerSupplier.INSTANCE.getLogger().warning("Ignoring exit request (debug mode)");
            }
            return dryExit ? AppStatus.RUNNING : AppStatus.EXITING; // New status
        });
        return oldStatus != AppStatus.EXITING;
    }
}
