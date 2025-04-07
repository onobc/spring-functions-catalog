/*
 * Copyright 2016-2025 the original author or authors.
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

package org.springframework.cloud.fn.consumer.zeromq;

import java.time.Duration;
import java.util.function.Function;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * @author Daniel Frey
 * @author Artem Bilan
 */
@SpringBootTest(properties = "zeromq.consumer.topic='test-topic'")
@DirtiesContext
public class ZeroMqConsumerConfigurationTests {

	private static final ZContext CONTEXT = new ZContext();

	private static ZMQ.Socket socket;

	private static int bindPort;

	@Autowired
	Function<Flux<Message<?>>, Mono<Void>> subject;

	@BeforeAll
	static void setup() {
		socket = CONTEXT.createSocket(SocketType.SUB);
		socket.setReceiveTimeOut(1000);
		bindPort = socket.bindToRandomPort("tcp://*");
	}

	@DynamicPropertySource
	static void zeromqConnectUrl(DynamicPropertyRegistry dynamicPropertyRegistry) {
		dynamicPropertyRegistry.add("zeromq.consumer.connectUrl", () -> "tcp://localhost:" + bindPort);
	}

	@AfterAll
	static void tearDown() {
		socket.close();
		CONTEXT.close();
	}

	@Test
	void testMessageHandlerConfiguration() {
		Message<?> testMessage = MessageBuilder.withPayload("test").setHeader("topic", "test-topic").build();

		await().atMost(Duration.ofSeconds(20)).pollDelay(Duration.ofMillis(100)).untilAsserted(() -> {
			socket.subscribe("test-topic");
			// Give it a chance to subscribe
			Thread.sleep(200);
			subject.apply(Flux.just(testMessage)).subscribe();
			String topic = socket.recvStr();
			assertThat(topic).isEqualTo("test-topic");
			assertThat(socket.recvStr()).isEmpty();
			assertThat(socket.recvStr()).isEqualTo("test");
		});
	}

	@SpringBootApplication
	public static class ZeroMqConsumerTestApplication {

	}

}
