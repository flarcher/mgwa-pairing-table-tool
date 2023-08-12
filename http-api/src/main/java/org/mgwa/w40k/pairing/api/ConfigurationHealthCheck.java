package org.mgwa.w40k.pairing.api;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class ConfigurationHealthCheck extends HealthCheck {

	public ConfigurationHealthCheck(Environment environment, Logger logger) {
		this.environment = Objects.requireNonNull(environment);
		this.logger      = Objects.requireNonNull(logger);
	}

	private final Environment environment;
	private final Logger logger;

	private void printServerPortsAsync(Environment environment) {
		environment.lifecycle().addServerLifecycleListener(new ServerLifecycleListener() {

			@Override
			public void serverStarted(Server server) {
				getServerPorts(server)
					.forEach(port -> logger.info(String.format("Using port %d", port)));
			}
		});
	}

	private static Stream<Integer> getServerPorts(@Nullable Server server) {
		if (server == null) {
			return Stream.empty();
		}
		else {
			return Arrays.stream(server.getConnectors())
					.filter(connector -> connector instanceof ServerConnector)
					.map(connector -> ((ServerConnector) connector).getLocalPort());
		}
	}

	@Override
	protected Result check() throws Exception {
		return getServerPorts(environment.getAdminContext().getServer()).anyMatch(p -> p > 1000)
				? Result.healthy()
				: Result.unhealthy("Using system network port: forbidden");
	}
}
