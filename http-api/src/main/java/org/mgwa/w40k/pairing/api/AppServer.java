package org.mgwa.w40k.pairing.api;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.mgwa.w40k.pairing.api.resource.MatrixResource;
import org.mgwa.w40k.pairing.state.AppState;
import org.mgwa.w40k.pairing.util.LoggerSupplier;

import java.util.Objects;
import java.util.logging.Logger;

public class AppServer extends Application<AppConfiguration> {

    private static final Logger logger = LoggerSupplier.INSTANCE.getLogger();

    private final AppState state;
    private Environment environment;
    private ConfigurationHealthCheck configurationHealthCheck;

    public AppServer(AppState state) {
        this.state = Objects.requireNonNull(state);
    }

    @Override
    public String getName() {
        return "MGWA W40k Table Pairing Tool";
    }

    @Override
    public void initialize(Bootstrap<AppConfiguration> bootstrap) {
        logger.info("HTTP API initialized");
    }

    @Override
    public void run(AppConfiguration configuration, Environment environment) throws Exception {
        this.environment = environment;

        // ByPass CORS security filtering
        environment.jersey().register(new AllowAllOriginsResponseFilter());
        // Register API resources
        environment.jersey().register(new MatrixResource(state));

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
