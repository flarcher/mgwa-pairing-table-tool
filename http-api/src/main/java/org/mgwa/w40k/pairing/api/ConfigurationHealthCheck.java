package org.mgwa.w40k.pairing.api;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class ConfigurationHealthCheck extends HealthCheck {

	public ConfigurationHealthCheck(Environment environment, Logger logger) {
		this.logger      = Objects.requireNonNull(logger);
		getServerPortsAsync(environment);
	}

	//private final Environment environment;
	private final Logger logger;

	private final AtomicReference<List<Integer>> serverPorts = new AtomicReference<>();

	private void getServerPortsAsync(Environment environment) {
		environment.lifecycle().addServerLifecycleListener(new ServerLifecycleListener() {

			@Override
			public void serverStarted(Server server) {
				List<Integer> ports = getServerPorts(server)
					.peek(port -> logger.info(String.format("Using port %d", port)))
					.toList();
				serverPorts.set(ports);
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

	Optional<List<Integer>> getServerPorts() {
		return Optional.ofNullable(serverPorts.get());
	}

	@Override
	protected Result check() throws Exception {
		return getServerPorts().orElse(Collections.emptyList()).stream()
			.anyMatch(p -> p < 1000)
				? Result.healthy()
				: Result.unhealthy("Using system network port: forbidden");
	}
}
