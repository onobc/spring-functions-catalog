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

package org.springframework.cloud.fn.consumer.analytics;

import java.util.Collection;

import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.Test;

import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Christian Tzolov
 */
@TestPropertySource(properties = { "analytics.name=counter666", "analytics.tag.fixed.foo=",
		"analytics.tag.expression.tag666=#jsonPath(payload,'$..noField')",
		"analytics.tag.expression.test=#jsonPath(payload,'$..test')" })
class EmptyTagsTests extends AnalyticsConsumerParentTests {

	@Test
	void testCounterSink() {

		analyticsConsumer.accept(message("{\"test\": \"Bar\"}"));

		Collection<Counter> fixedTagsCounters = meterRegistry.find("counter666").tagKeys("foo").counters();
		assertThat(fixedTagsCounters.size()).isEqualTo(0);

		Collection<Counter> expressionTagsCounters = meterRegistry.find("counter666").tagKeys("tag666").counters();
		assertThat(expressionTagsCounters.size()).isEqualTo(1);
		assertThat(meterRegistry.find("counter666").meter().getId().getTag("tag666"))
			.isEqualTo(AnalyticsConsumerConfiguration.UNAVAILABLE_TAG);

		Collection<Counter> testExpTagsCounters = meterRegistry.find("counter666").tagKeys("test").counters();
		assertThat(testExpTagsCounters.size()).isEqualTo(1);
		assertThat(meterRegistry.find("counter666").meter().getId().getTag("test")).isEqualTo("Bar");
	}

}
