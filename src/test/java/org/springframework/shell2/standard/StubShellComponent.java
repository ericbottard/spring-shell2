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

package org.springframework.shell2.standard;

import java.util.List;

import org.springframework.shell2.result.ExitResult;

/**
 * An example commands class.
 *
 * @author Eric Bottard
 * @author Florent Biville
 * @author Camilo Gonzalez
 */
@ShellComponent
class StubShellComponent {

	/**
	 * A command method that showcases
	 * <ul>
	 * <li>default handling for booleans (force)</li>
	 * <li>default parameter name discovery (name)</li>
	 * <li>default value supplying (foo and bar)</li>
	 * </ul>
	 */
	@ShellMethod(help = "change de chaine")
	public void zap(boolean force, String name, @ShellOption(defaultValue = "defoolt") String foo,
			@ShellOption(value = { "--bar", "--baz" }, defaultValue = "last") String bar) {
	}

	@ShellMethod(help = "interactive")
	public void interactive(@ShellOption(interactive = true) String value) {
	}
	
	@ShellMethod(help = "nonInteractive")
	public void nonInteractive(@ShellOption String foo, String bar) {
	}

	@ShellMethod(help = "bye bye")
	public ExitResult shutdown(@ShellOption String delay) {
		return new ExitResult(1);
	}

	@ShellMethod(help = "sans les doigts !")
	public String add(@ShellOption(arity = 3) List<Integer> numbers) {
		return numbers.stream().reduce(0, (a, b) -> {
			return a + b;
		}).toString();
	}
}
