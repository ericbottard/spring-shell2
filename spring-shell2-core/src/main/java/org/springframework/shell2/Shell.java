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

package org.springframework.shell2;

import java.util.Map;

/**
 * Implementing this interface allows sub-systems (such as the {@literal help} command) to discover
 * available commands.
 *
 * @author Eric Bottard
 * @author Camilo Gonzalez
 */
public interface Shell {

	/**
	 * Return the mapping from command trigger keywords to implementation.
	 */
	public Map<String, MethodTarget> listCommands();

	/**
	 * Allows sub-systems to request lines for interactive input with the user.
	 * 
	 * @param prompt
	 *            message to be displayed to the user for requesting user input
	 * @param masked
	 *            defines if the input should be masked or not
	 * @return the line read, or null if the operation was cancelled
	 */
	public String readLine(String prompt, boolean masked);
}
