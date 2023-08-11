package org.mgwa.w40k.pairing.api;

import io.dropwizard.Application;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.mgwa.w40k.pairing.api.resource.MatrixResource;
import org.mgwa.w40k.pairing.state.AppState;
import org.mgwa.w40k.pairing.util.LoggerSupplier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class AppServer extends Application<PairingConfiguration> {

    private static final Logger logger = LoggerSupplier.INSTANCE.getLogger();

    private final AppState state;
    private Environment environment;

    public AppServer(AppState state) {
        this.state = Objects.requireNonNull(state);
    }

    @Override
    public String getName() {
        return "MGWA W40k Table Pairing Tool";
    }

    @Override
    public void initialize(Bootstrap<PairingConfiguration> bootstrap) {
        logger.info("HTTP API initialized");
    }

    private void printServerPortsAsync(Environment environment) {
        environment.lifecycle().addServerLifecycleListener(new ServerLifecycleListener() {

            @Override
            public void serverStarted(Server server) {
                List<Integer> serverPorts = new ArrayList<>();
                Arrays.stream(server.getConnectors())
                        .filter(connector -> connector instanceof ServerConnector)
                        .map(connector -> ((ServerConnector) connector).getLocalPort())
                        .forEach(port -> logger.info(String.format("Using port %d", port)));

            }
        });
    }

    @Override
    public void run(PairingConfiguration configuration, Environment environment) throws Exception {
        this.environment = environment;

        // ByPass CORS security filtering
        environment.jersey().register(new AllowAllOriginsResponseFilter());
        // Register API resources
        environment.jersey().register(new MatrixResource());
    }

    public void stop() throws Exception {
        environment.getApplicationContext().getServer().stop();
    }
}
