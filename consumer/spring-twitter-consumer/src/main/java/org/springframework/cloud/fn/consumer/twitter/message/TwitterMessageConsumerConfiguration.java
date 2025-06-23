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

package org.springframework.cloud.fn.consumer.twitter.message;

import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.fn.common.twitter.TwitterConnectionConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

/**
 * The auto-configuration for Twitter messages.
 *
 * @author Christian Tzolov
 * @author Artem Bilan
 */
@AutoConfiguration(after = TwitterConnectionConfiguration.class)
@EnableConfigurationProperties(TwitterMessageConsumerProperties.class)
@ConditionalOnExpression("environment['twitter.message.update.user-id'] !='' or environment['twitter.message.update.screen-name'] != ''")
public class TwitterMessageConsumerConfiguration {

	private static final Log LOGGER = LogFactory.getLog(TwitterMessageConsumerConfiguration.class);

	@Bean
	public Consumer<Message<?>> twitterSendMessageConsumer(TwitterMessageConsumerProperties messageProperties,
			Twitter twitter) {

		return (message) -> {
			try {
				String messageText = messageProperties.getText().getValue(message, String.class);

				if (messageProperties.getUserId() != null) {
					Long userId = messageProperties.getUserId().getValue(message, long.class);
					if (messageProperties.getMediaId() != null) {
						Long mediaId = messageProperties.getMediaId().getValue(message, long.class);
						twitter.sendDirectMessage(userId, messageText, mediaId);
					}
					twitter.sendDirectMessage(userId, messageText);
				}
				else if (messageProperties.getScreenName() != null) {
					String screenName = messageProperties.getScreenName().getValue(message, String.class);
					twitter.sendDirectMessage(screenName, messageText);
				}
				else {
					throw new RuntimeException("Either the UserId or screenName must be set");
				}
			}
			catch (TwitterException ex) {
				LOGGER.error("Failed to process message: " + message, ex);
			}
		};
	}

}
