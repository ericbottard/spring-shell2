/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.shell2.result;

import java.io.Closeable;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link ExitResult} objects that will cause the application to terminate.
 * 
 * @author Camilo Gonzalez
 */
@Component
public class ExitResultHandler implements ResultHandler<ExitResult> {

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public void handleResult(ExitResult result) {
		System.out.println("Terminating the shell");
		if (applicationContext instanceof Closeable) {
			try {
				((Closeable) applicationContext).close();
			} catch (IOException ex) {
				System.err.println("Exception whilst closing the application context: " + ex);
			}
			System.exit(result.status());
		}
	}
}
