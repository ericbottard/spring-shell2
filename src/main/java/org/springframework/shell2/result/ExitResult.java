/*
 * Copyright 2015-2017 the original author or authors.
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

/**
 * This result will ask the shell to gracefully shutdown.
 *
 * @author Eric Bottard
 * @author Camilo Gonzalez
 */
public class ExitResult {

	private final int code;

	public ExitResult() {
		this(0);
	}

	public ExitResult(int code) {
		this.code = code;
	}

	public int status() {
		return code;
	}
}
