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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ReflectionUtils.findMethod;

import java.lang.reflect.Method;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.shell2.ParameterResolutionException;
import org.springframework.shell2.Utils;

/**
 * Unit tests for DefaultParameterResolver.
 *
 * @author Eric Bottard
 * @author Florent Biville
 */
public class StandardParameterResolverTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private StandardParameterResolver resolver = new StandardParameterResolver(new DefaultConversionService());

	@Test
	public void testParses() throws Exception {
		Method method = findMethod(Remote.class, "zap", boolean.class, String.class, String.class, String.class);

		assertThat(resolver.resolve(
				Utils.createMethodParameter(method, 0),
				asList("--force --name --foo y".split(" "))
		)).isEqualTo(true);
		assertThat(resolver.resolve(
				Utils.createMethodParameter(method, 1),
				asList("--force --name --foo y".split(" "))
		)).isEqualTo("--foo");
		assertThat(resolver.resolve(
				Utils.createMethodParameter(method, 2),
				asList("--force --name --foo y".split(" "))
		)).isEqualTo("y");
		assertThat(resolver.resolve(
				Utils.createMethodParameter(method, 3),
				asList("--force --name --foo y".split(" "))
		)).isEqualTo("last");

	}

	@Test
	public void testParameterSpecifiedTwiceViaDifferentAliases() throws Exception {
		Method method = findMethod(Remote.class, "zap", boolean.class, String.class, String.class, String.class);

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Named parameter has been specified multiple times via '--bar, --baz'");

		resolver.resolve(
				Utils.createMethodParameter(method, 0),
				asList("--force --name --foo y --bar x --baz z".split(" "))
		);
	}

	@Test
	public void testParameterSpecifiedTwiceViaSameKey() throws Exception {
		Method method = findMethod(Remote.class, "zap", boolean.class, String.class, String.class, String.class);

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Parameter for '--baz' has already been specified");

		resolver.resolve(
				Utils.createMethodParameter(method, 0),
				asList("--force --name --foo y --baz x --baz z".split(" "))
		);
	}

	@Test
	public void testUnknownParameter() throws Exception {
		Method method = findMethod(Remote.class, "zap", boolean.class, String.class, String.class, String.class);

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Could not look up parameter for '--unknown' in " + method);

		resolver.resolve(
				Utils.createMethodParameter(method, 0),
				asList("--unknown --foo bar".split(" "))
		);
	}

	@Test
	public void testTooMuchInput() throws Exception {
		Method method = findMethod(Remote.class, "zap", boolean.class, String.class, String.class, String.class);

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("the following could not be mapped to parameters: 'leftover'");

		resolver.resolve(
				Utils.createMethodParameter(method, 0),
				asList("--foo hello --name bar --force --bar well leftover".split(" "))
		);
	}

	@Test
	public void testIncompleteCommandResolution() throws Exception {
		Method method = findMethod(Remote.class, "shutdown", String.class);

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Not enough input for parameter '--delay'");

		resolver.resolve(
				Utils.createMethodParameter(method, 0),
				asList("--delay".split(" "))
		);
	}

	@Test
	public void testUnresolvableArg() throws Exception {
		Method method = findMethod(Remote.class, "zap", boolean.class, String.class, String.class, String.class);

		thrown.expect(ParameterResolutionException.class);
		thrown.expectMessage("Parameter '--name string' should be specified");

		resolver.resolve(
				Utils.createMethodParameter(method, 1),
				asList("--foo hello --force --bar well".split(" "))
		);

	}
}
