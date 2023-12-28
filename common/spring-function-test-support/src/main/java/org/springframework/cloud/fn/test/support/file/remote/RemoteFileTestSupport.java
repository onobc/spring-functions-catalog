/*
 * Copyright 2015-2016 the original author or authors.
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

package org.springframework.cloud.fn.test.support.file.remote;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

/**
 * Abstract base class for tests requiring remote file servers, e.g. (S)FTP.
 *
 * @author Gary Russell
 *
 */
public abstract class RemoteFileTestSupport {

	protected static final int port = 0;

	@TempDir
	protected static Path remoteTemporaryFolder;

	@TempDir
	protected static Path localTemporaryFolder;

	protected volatile File sourceRemoteDirectory;

	protected volatile File targetRemoteDirectory;

	protected volatile File sourceLocalDirectory;

	protected volatile File targetLocalDirectory;

	public File getSourceRemoteDirectory() {
		return sourceRemoteDirectory;
	}

	public File getTargetRemoteDirectory() {
		return targetRemoteDirectory;
	}

	public File getSourceLocalDirectory() {
		return sourceLocalDirectory;
	}

	public File getTargetLocalDirectory() {
		return targetLocalDirectory;
	}

	/**
	 * Default implementation creates the following folder structures:
	 *
	 * <pre class="code">
	 *  $ tree remoteSource/
	 *  remoteSource/
	 *  ├── remoteSource1.txt - contains 'source1'
	 *  ├── remoteSource2.txt - contains 'source2'
	 *  remoteTarget/
	 *  $ tree localSource/
	 *  localSource/
	 *  ├── localSource1.txt - contains 'local1'
	 *  ├── localSource2.txt - contains 'local2'
	 *  localTarget/
	 * </pre>
	 *
	 * The intent is tests retrieve from remoteSource and verify arrival in localTarget or send from localSource and verify
	 * arrival in remoteTarget.
	 * <p>
	 * Subclasses can change 'remote' in these names by overriding {@link #prefix()} or override this method completely to
	 * create a different structure.
	 * <p>
	 * While a single server exists for all tests, the directory structure is rebuilt for each test.
	 * @throws IOException IO Exception.
	 */
	@BeforeEach
	public void setupFolders() throws IOException {
		String prefix = prefix();
		recursiveDelete(new File(remoteTemporaryFolder.toFile(), prefix + "Source"));

		sourceRemoteDirectory = new File(remoteTemporaryFolder.toFile(), prefix + "Source");
		sourceRemoteDirectory.mkdirs();
		recursiveDelete(new File(remoteTemporaryFolder.toFile(), prefix + "Target"));
		targetRemoteDirectory = new File(remoteTemporaryFolder.toFile(), prefix + "Target");
		targetRemoteDirectory.mkdirs();
		recursiveDelete(new File(localTemporaryFolder.toFile(), "localSource"));
		sourceLocalDirectory = new File(localTemporaryFolder.toFile(), "localSource");
		sourceLocalDirectory.mkdirs();
		recursiveDelete(new File(localTemporaryFolder.toFile(), "localTarget"));
		targetLocalDirectory = new File(localTemporaryFolder.toFile(), "localTarget");
		targetLocalDirectory.mkdirs();
		File file = new File(sourceRemoteDirectory, prefix + "Source1.txt");
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write("source1".getBytes());
		fos.close();
		file = new File(sourceRemoteDirectory, prefix + "Source2.txt");
		file.createNewFile();
		fos = new FileOutputStream(file);
		fos.write("source2".getBytes());
		fos.close();
		file = new File(sourceLocalDirectory, "localSource1.txt");
		file.createNewFile();
		fos = new FileOutputStream(file);
		fos.write("local1".getBytes());
		fos.close();
		file = new File(sourceLocalDirectory, "localSource2.txt");
		file.createNewFile();
		fos = new FileOutputStream(file);
		fos.write("local2".getBytes());
		fos.close();
	}

	public static void recursiveDelete(File file) {
		if (file != null && file.exists()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File fyle : files) {
					if (fyle.isDirectory()) {
						recursiveDelete(fyle);
					}
					else {
						fyle.delete();
					}
				}
			}
			file.delete();
		}
	}

	/**
	 * Prefix for directory/file structure; default 'remote'.
	 * @return the prefix.
	 */
	protected String prefix() {
		return "remote";
	}
}
