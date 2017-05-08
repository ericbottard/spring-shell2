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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ReflectionUtils.findMethod;

import java.lang.reflect.Method;
import java.util.List;

import org.jline.reader.ParsedLine;
import org.jline.reader.impl.DefaultParser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.shell2.CompletionContext;
import org.springframework.shell2.ParameterMissingResolutionException;
import org.springframework.shell2.UnfinishedParameterResolutionException;
import org.springframework.shell2.Utils;

/**
 * Unit tests for the {@link StandardParameterResolver}.
 * 
 * @author Eric Bottard
 * @author Florent Biville
 * @author Camilo Gonzalez
 */
public class StandardParameterResolverTest {

	private static final String ADD_METHOD = "add";

	private static final String ZAP_METHOD = "zap";
	
	private static final String INTERACTIVE_METHOD = "interactive";
	
	private static final String NON_INTERACTIVE_METHOD = "nonInteractive";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private StandardParameterResolver resolver = new StandardParameterResolver(new DefaultConversionService());

	// Tests for resolution

	@Test
	public void testParses() throws Exception {
		Method method = findMethod(StubShellComponent.class, ZAP_METHOD, boolean.class, String.class, String.class, String.class);

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
		Method method = findMethod(StubShellComponent.class, ZAP_METHOD, boolean.class, String.class, String.class, String.class);

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Named parameter has been specified multiple times via '--bar, --baz'");

		resolver.resolve(
				Utils.createMethodParameter(method, 0),
				asList("--force --name --foo y --bar x --baz z".split(" "))
		);
	}

	@Test
	public void testParameterSpecifiedTwiceViaSameKey() throws Exception {
		Method method = findMethod(StubShellComponent.class, ZAP_METHOD, boolean.class, String.class, String.class, String.class);

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Parameter for '--baz' has already been specified");

		resolver.resolve(
				Utils.createMethodParameter(method, 0),
				asList("--force --name --foo y --baz x --baz z".split(" "))
		);
	}

	@Test
	public void testTooMuchInput() throws Exception {
		Method method = findMethod(StubShellComponent.class, ZAP_METHOD, boolean.class, String.class, String.class, String.class);

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("the following could not be mapped to parameters: 'leftover'");

		resolver.resolve(
				Utils.createMethodParameter(method, 0),
				asList("--foo hello --name bar --force --bar well leftover".split(" "))
		);
	}

	@Test
	public void testIncompleteCommandResolution() throws Exception {
		Method method = findMethod(StubShellComponent.class, "shutdown", String.class);

		thrown.expect(UnfinishedParameterResolutionException.class);
		thrown.expectMessage("Error trying to resolve '--delay string' using [--delay]");

		resolver.resolve(
				Utils.createMethodParameter(method, 0),
				asList("--delay".split(" "))
		);
	}

	@Test
	public void testIncompleteCommandResolutionBigArity() throws Exception {
		Method method = findMethod(StubShellComponent.class, ADD_METHOD, List.class);

		thrown.expect(UnfinishedParameterResolutionException.class);
		thrown.expectMessage("Error trying to resolve '--numbers list list list' using [--numbers 1 2]");

		resolver.resolve(
				Utils.createMethodParameter(method, 0),
				asList("--numbers 1 2".split(" "))
		);
	}

	@Test
	public void testUnresolvableArg() throws Exception {
		Method method = findMethod(StubShellComponent.class, ZAP_METHOD, boolean.class, String.class, String.class, String.class);

		thrown.expect(ParameterMissingResolutionException.class);
		thrown.expectMessage("Parameter '--name string' should be specified");

		resolver.resolve(
				Utils.createMethodParameter(method, 1),
				asList("--foo hello --force --bar well".split(" "))
		);
	}

	// Tests for completion

	@Test
	public void testParameterKeyNotYetSetAppearsInProposals() {
		Method method = findMethod(StubShellComponent.class, ZAP_METHOD, boolean.class, String.class, String.class, String.class);
		List<String> completions = resolver.complete(
				Utils.createMethodParameter(method, 1),
				contextFor("")
		);
		assertThat(completions).contains("--name");
		completions = resolver.complete(
				Utils.createMethodParameter(method, 1),
				contextFor("--force ")
		);
		assertThat(completions).contains("--name");
	}

	@Test
	public void testParameterKeyNotFullySpecified() {
		Method method = findMethod(StubShellComponent.class, ZAP_METHOD, boolean.class, String.class, String.class, String.class);
		List<String> completions = resolver.complete(
				Utils.createMethodParameter(method, 1),
				contextFor("--na")
		);
		assertThat(completions).contains("--name");
		completions = resolver.complete(
				Utils.createMethodParameter(method, 1),
				contextFor("--force --na")
		);
		assertThat(completions).contains("--name");
	}

	@Test
	public void testNoMoreAvailableParameters() {
		Method method = findMethod(StubShellComponent.class, ZAP_METHOD, boolean.class, String.class, String.class, String.class);
		List<String> completions = resolver.complete(
				Utils.createMethodParameter(method, 2), // trying to complete --foo
				contextFor("--name ") // but input is currently focused on --name
		);
		System.out.println(completions);
//		assertThat(completions).isEmpty();
	}

	@Test
	public void testNotTheRightTimeToCompleteThatParameter() {
		Method method = findMethod(StubShellComponent.class, "shutdown", String.class);
		List<String> completions = resolver.complete(
				Utils.createMethodParameter(method, 0),
				contextFor("--delay 323")
		);
		assertThat(completions).isEmpty();
	}

	@Test
	public void testValueCompletionWithNonDefaultArity() {
		Method method = findMethod(StubShellComponent.class, ADD_METHOD, List.class);
		List<String> completions = resolver.complete(
				Utils.createMethodParameter(method, 0),
				contextFor("--numbers ")
		);
		assertThat(completions).contains("12");

		completions = resolver.complete(
				Utils.createMethodParameter(method, 0),
				contextFor("--numbers 42 ")
		);
		assertThat(completions).contains("12");

		completions = resolver.complete(
				Utils.createMethodParameter(method, 0),
				contextFor("--numbers 42 34 ")
		);
		assertThat(completions).contains("12");

		completions = resolver.complete(
				Utils.createMethodParameter(method, 0),
				contextFor("--numbers 42 34 66 ")
		);
		assertThat(completions).isEmpty();
	}
	
	@Test
	public void testNotSupportedParameter() {
		Method method = findMethod(StubShellComponent.class, INTERACTIVE_METHOD, String.class);
		
		assertThat(resolver.supports(Utils.createMethodParameter(method, 0))).isFalse();
	}
	
	@Test
	public void testSupportedParameters() {
		Method method = findMethod(StubShellComponent.class, NON_INTERACTIVE_METHOD, String.class, String.class);
		
		assertThat(resolver.supports(Utils.createMethodParameter(method, 0))).isTrue();
		assertThat(resolver.supports(Utils.createMethodParameter(method, 1))).isTrue();
	}

	private CompletionContext contextFor(String input) {
		DefaultParser defaultParser = new DefaultParser();
		ParsedLine parsed = defaultParser.parse(input, input.length());
		return new CompletionContext(parsed.words(), parsed.wordIndex(), parsed.wordCursor());
	}
}
