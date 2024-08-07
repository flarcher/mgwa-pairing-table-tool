package org.mgwa.w40k.pairing.api.service;

import org.mgwa.w40k.pairing.api.model.AppStatus;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Handle the API REST lifecycle.
 */
public class StatusService {

    public StatusService(Runnable onExit) {
        this.onExit = Objects.requireNonNull(onExit);
    }

    private final Runnable onExit;
    private final AtomicReference<AppStatus> currentStatus = new AtomicReference<>(AppStatus.INITIALIZING);

    public AppStatus getStatus() {
        return currentStatus.get();
    }

    public boolean initialized() {
        return currentStatus.compareAndSet(AppStatus.INITIALIZING, AppStatus.RUNNING);
    }

    public boolean exiting() {
        AppStatus oldStatus = currentStatus.getAndUpdate( status -> {
            if (status != AppStatus.EXITING) {
                onExit.run();
            }
            return AppStatus.EXITING; // New status
        });
        return oldStatus != AppStatus.EXITING;
    }
}
