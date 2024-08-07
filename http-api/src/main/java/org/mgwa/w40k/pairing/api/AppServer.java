package org.mgwa.w40k.pairing.api;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.mgwa.w40k.pairing.api.resource.AnalysisResource;
import org.mgwa.w40k.pairing.api.resource.AppResource;
import org.mgwa.w40k.pairing.api.resource.MatrixResource;
import org.mgwa.w40k.pairing.api.service.PairingService;
import org.mgwa.w40k.pairing.api.service.StatusService;
import org.mgwa.w40k.pairing.state.AppState;
import org.mgwa.w40k.pairing.util.LoggerSupplier;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppServer extends Application<AppConfiguration> {

    private static final Logger logger = LoggerSupplier.INSTANCE.getLogger();

    private final AppState state;
    private final Runnable onExit;
    private Environment environment;
    private ConfigurationHealthCheck configurationHealthCheck;

    public AppServer(AppState state, Runnable onExit) {
        this.state = Objects.requireNonNull(state);
        this.onExit = Objects.requireNonNull(onExit);
    }

    @Override
    public String getName() {
        return "MGWA W40k Table Pairing Tool";
    }

    @Override
    public void initialize(Bootstrap<AppConfiguration> bootstrap) {
        logger.info("HTTP API initialized");
    }

    private void stopFromAnotherTread() {
        // It is required, otherwise we get a deadlock, since the server waits for the calling request completion
        Executors.defaultThreadFactory().newThread(() -> {
            try {
                logger.info("Stopping server");
                AppServer.this.stop();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Unable to stop server", t);
            }
        }).start();
    }

    @Override
    public void run(AppConfiguration configuration, Environment environment) throws Exception {
        this.environment = environment;

        // Services
        PairingService pairingService = new PairingService(state);
        StatusService statusService = new StatusService(() -> {
            logger.info("Scheduling stop");
            stopFromAnotherTread();
            logger.info("Calling exit callback");
            onExit.run();
        });

        // ByPass CORS security filtering
        environment.jersey().register(new AllowAllOriginsResponseFilter());
        // Register API resources
        environment.jersey().register(new MatrixResource(state, pairingService));
        environment.jersey().register(new AnalysisResource(pairingService));
        environment.jersey().register(new AppResource(statusService));

        // Health checks
        configurationHealthCheck = new ConfigurationHealthCheck(environment, logger);
        environment.healthChecks().register("configuration", configurationHealthCheck);
    }

    public void stop() throws Exception {
        environment.getApplicationContext().getServer().stop();
    }

    /**
     * @return The first known server port or {@code -1} if unknown.
     */
    public int getServerPort() {
        return configurationHealthCheck.getServerPorts()
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0))
                .orElse(-1);
    }
}
