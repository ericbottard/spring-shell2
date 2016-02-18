/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.shell2.standard;

import java.util.List;

/**
 * An example commands class.
 *
 * @author Eric Bottard
 * @author Florent Biville
 */
@ShellComponent
class Remote2 {

	/**
	 * A command method that showcases<ul>
	 *     <li>default handling for booleans (force)</li>
	 *     <li>default parameter name discovery (name)</li>
	 *     <li>default value supplying (foo and bar)</li>
	 * </ul>
	 */
	@ShellMethod(help = "change de chaine")
	public void zap(boolean force,
	                String name,
	                @ShellOption(defaultValue="defoolt") String foo,
	                @ShellOption(value = {"--bar", "--baz"}, defaultValue = "last") String bar) {

	}

	@ShellMethod(help = "bye bye")
	public void shutdown(@ShellOption String delay) {

	}

	@ShellMethod(help = "sans les doigts !")
	public void add(@ShellOption(arity = 3) List<Integer> numbers) {

	}
}
