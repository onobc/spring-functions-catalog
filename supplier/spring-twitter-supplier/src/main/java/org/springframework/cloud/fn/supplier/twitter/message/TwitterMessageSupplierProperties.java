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

package org.springframework.cloud.fn.supplier.twitter.message;

import jakarta.validation.constraints.Max;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * The Twitter supplier messages receiving properties.
 *
 * @author Christian Tzolov
 * @author Artem Bilan
 */
@ConfigurationProperties("twitter.message.source")
@Validated
public class TwitterMessageSupplierProperties {

	/**
	 * Whether to enable Twitter message receiving.
	 */
	private boolean enabled;

	/**
	 * Max number of events to be returned. 20 default. 50 max.
	 */
	@Max(50)
	private int count = 20;

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
