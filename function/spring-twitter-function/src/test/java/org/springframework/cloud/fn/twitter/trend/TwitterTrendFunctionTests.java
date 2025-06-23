/*
 * Copyright 2020-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.fn.twitter.trend;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import twitter4j.conf.ConfigurationBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.fn.common.twitter.TwitterConnectionProperties;
import org.springframework.cloud.fn.common.twitter.util.TwitterTestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.once;

/**
 * @author Christian Tzolov
 * @author Artem Bilan
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = { "twitter.connection.consumerKey=consumerKey666",
				"twitter.connection.consumerSecret=consumerSecret666", "twitter.connection.accessToken=accessToken666",
				"twitter.connection.accessTokenSecret=accessTokenSecret666" })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class TwitterTrendFunctionTests {

	private static ClientAndServer mockServer;

	private static MockServerClient mockClient;

	private static HttpRequest trendsRequest;

	@Autowired
	protected Function<Message<?>, Message<byte[]>> twitterTrendFunction;

	@BeforeAll
	public static void startServer() {
		mockServer = ClientAndServer.startClientAndServer();
		mockClient = new MockServerClient("localhost", mockServer.getPort());

		trendsRequest = setExpectation(
				request().withMethod("GET").withPath("/trends/place.json").withQueryStringParameter("id", "2972"));
	}

	@AfterAll
	public static void stopServer() {
		mockServer.stop();
	}

	public static HttpRequest setExpectation(HttpRequest request) {
		mockClient.when(request, exactly(1))
			.respond(response().withStatusCode(200)
				.withHeaders(new Header("Content-Type", "application/json; charset=utf-8"),
						new Header("Cache-Control", "public, max-age=86400"))
				.withBody(TwitterTestUtils.asString("classpath:/response/trends.json"))
				.withDelay(TimeUnit.SECONDS, 1));
		return request;
	}

	@TestPropertySource(properties = { "twitter.trend.trendQueryType=trend", "twitter.trend.locationId='2972'",
			"twitter.connection.rawJson=true" })
	public static class TwitterTrendPayloadTests extends TwitterTrendFunctionTests {

		@Test
		public void testOne() {
			Message<?> received = twitterTrendFunction.apply(MessageBuilder.withPayload("Hello").build());
			mockClient.verify(trendsRequest, once());
			assertThat(received).isNotNull();
		}

	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	public static class TwitterTrendFunctionTestApplication {

		@Bean
		@Primary
		public twitter4j.conf.Configuration twitterConfiguration2(TwitterConnectionProperties properties,
				Function<TwitterConnectionProperties, ConfigurationBuilder> toConfigurationBuilder) {

			Function<TwitterConnectionProperties, ConfigurationBuilder> mockedConfiguration = toConfigurationBuilder
				.andThen(new TwitterTestUtils()
					.mockTwitterUrls(String.format("http://localhost:" + mockServer.getPort())));

			return mockedConfiguration.apply(properties).build();
		}

	}

}
