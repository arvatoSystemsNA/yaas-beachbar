package us.arvatosystems.com.yaas;

import javax.ws.rs.client.ClientBuilder;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.cloud.yaas.api.ArvatoSmsServiceClient;
import com.sap.cloud.yaas.api.HybrisPubSubServiceApiClient;
import com.sap.cloud.yaas.api.OrderServiceClient;
import com.sap.cloud.yaas.servicesdk.jerseysupport.logging.RequestResponseLoggingFilter;

@Configuration
@EnableAsync
@EnableScheduling
public class SpringApplication
{
	private static final org.slf4j.Logger REQUEST_RESPONSE_LOG = LoggerFactory
			.getLogger("us.arvatosystems.com.yaas.net.request_response");

	@Value("${TENANT}")
	private String tenant;

	@Value("${SMS_SERVICE_ENDPOINT_URL}")
	private String smsServiceBaseUri;

	@Bean
	public ArvatoSmsServiceClient createArvatoSmsServiceClient()
	{
		final ArvatoSmsServiceClient client = new ArvatoSmsServiceClient(smsServiceBaseUri,
				ClientBuilder.newClient(createClientConfig())).withUriParam("tenant", tenant);

		return client;
	}

	@Bean
	public HybrisPubSubServiceApiClient createHybrisPubSubServiceApiClient()
	{
		final HybrisPubSubServiceApiClient client = new HybrisPubSubServiceApiClient(HybrisPubSubServiceApiClient.DEFAULT_BASE_URI,
				ClientBuilder.newClient(createClientConfig()));

		return client;
	}

	@Bean
	public OrderServiceClient createOrderServiceClient()
	{
		return new OrderServiceClient(OrderServiceClient.DEFAULT_BASE_URI, ClientBuilder.newClient(createClientConfig()));
	}

	@Bean
	public ObjectMapper createObjectMapper()
	{
		final ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		return mapper;
	}

	private ClientConfig createClientConfig()
	{
		// connection pooling
		final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();

		// config based on Apache http client
		final ClientConfig clientConfig = new ClientConfig();
		clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER, cm);
		clientConfig.property(ApacheClientProperties.REQUEST_CONFIG, RequestConfig.DEFAULT);
		clientConfig.connectorProvider(new ApacheConnectorProvider());

		// with logging
		clientConfig.register(new RequestResponseLoggingFilter(REQUEST_RESPONSE_LOG, 999999));

		return clientConfig;
	}
}
