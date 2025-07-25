/*
 * Copyright 2015-present the original author or authors.
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

package org.springframework.cloud.fn.consumer.file;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.FileCopyUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mark Fisher
 * @author Artem Bilan
 * @author Soby Chacko
 * <p>
 * We don't need a separate SpringBootApplication for this test as there is already one
 * available in this package. {@link AbstractFileConsumerTests}.
 */
@SpringBootTest(properties = { "file.consumer.nameExpression = payload.substring(0, 4)",
		"file.consumer.directoryExpression = '${java.io.tmpdir}'+'/'+headers.dir", "file.consumer.suffix=out" })
@DirtiesContext
public class ExpressionTests {

	@TempDir
	static Path tempDir;

	@Autowired
	Consumer<Message<?>> fileConsumer;

	@Test
	public void test() throws Exception {
		fileConsumer.accept(MessageBuilder.withPayload("this is something").setHeader("dir", "expression").build());
		File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "expression", "this.out");
		file.deleteOnExit();
		assertThat(file.exists()).isTrue();
		assertThat("this is something" + System.lineSeparator())
			.isEqualTo(FileCopyUtils.copyToString(new FileReader(file)));
	}

}
