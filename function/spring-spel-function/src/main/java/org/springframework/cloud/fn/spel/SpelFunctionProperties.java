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

package org.springframework.cloud.fn.spel;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.Expression;
import org.springframework.integration.expression.FunctionExpression;
import org.springframework.messaging.Message;

/**
 * Configuration properties for the SpEL function.
 *
 * @author Gary Russell
 * @author Artem Bilan
 */
@ConfigurationProperties("spel.function")
public class SpelFunctionProperties {

	private static final Expression DEFAULT_EXPRESSION = new FunctionExpression<Message<?>>(Message::getPayload);

	/**
	 * A SpEL expression to apply.
	 */
	private Expression expression = DEFAULT_EXPRESSION;

	public Expression getExpression() {
		return this.expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

}
