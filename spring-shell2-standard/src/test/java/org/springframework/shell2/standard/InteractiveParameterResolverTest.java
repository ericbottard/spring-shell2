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

package org.springframework.shell2.standard;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.util.ReflectionUtils.findMethod;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.shell2.ParameterDescription;
import org.springframework.shell2.Shell;
import org.springframework.shell2.Utils;

/**
 * Unit tests for the {@link InteractiveParameterResolver}.
 * 
 * @author Camilo Gonzalez
 */
public class InteractiveParameterResolverTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	private Shell shell;
	
	private InteractiveParameterResolver resolver = new InteractiveParameterResolver(new DefaultConversionService());

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		resolver.setShell(shell);
	}
	
	// Tests for resolution

	@Test
	public void testParses() throws Exception {
		String expectedPassword = "passw0rd";
		when(shell.readLine("password:> ", true)).thenReturn(expectedPassword);
		
		Method method = findMethod(Remote.class, "authenticate", String.class, String.class);

		assertThat(resolver.resolve(
				Utils.createMethodParameter(method, 1),
				asList("--username abc".split(" "))
		)).isEqualTo(expectedPassword);
	}
	
	@Test
	public void testParsesEmptyNoDefault() throws Exception {
		String expectedPassword = "";
		when(shell.readLine("password:> ", true)).thenReturn(expectedPassword);
		
		Method method = findMethod(Remote.class, "authenticate", String.class, String.class);

		assertThat(resolver.resolve(
				Utils.createMethodParameter(method, 1),
				asList("--username abc".split(" "))
		)).isEqualTo(expectedPassword);
	}
	
	@Test
	public void testParsesEmptyWithDefaultGiven() throws Exception {
		String expectedValue = "default";
		when(shell.readLine("value (default):> ", true)).thenReturn("");
		
		Method method = findMethod(Remote.class, "interactiveWithDefault", String.class);

		assertThat(resolver.resolve(
				Utils.createMethodParameter(method, 0),
				asList("".split(" "))
		)).isEqualTo(expectedValue);
	}
	
	// Tests for describe
	
	@Test
	public void testDescribeWithDefault() {
		Method method = findMethod(Remote.class, "interactiveWithDefault", String.class);
		MethodParameter methodParameter = Utils.createMethodParameter(method, 0);
		ShellOption option = methodParameter.getParameterAnnotation(ShellOption.class);

		ParameterDescription description = resolver.describe(methodParameter);
		assertThat(description.defaultValue().get()).isEqualTo(option.defaultValue());
		assertThat(description.mandatoryKey()).isFalse();
		assertThat(description.formal()).isEmpty();
		assertThat(description.help()).isEqualTo(option.help());
		assertThat(description.keys().size()).isEqualTo(1);

		String keyPrefix = "--";
		assertThat(description.keys().iterator().next()).isEqualTo(keyPrefix + methodParameter.getParameterName());
	}
	
	@Test
	public void testDescribeWithNoDefault() {
		Method method = findMethod(Remote.class, "interactive", String.class);
		MethodParameter methodParameter = Utils.createMethodParameter(method, 0);
		ShellOption option = methodParameter.getParameterAnnotation(ShellOption.class);

		ParameterDescription description = resolver.describe(methodParameter);
		assertThat(description.defaultValue().isPresent()).isFalse();
		assertThat(description.mandatoryKey()).isFalse();
		assertThat(description.formal()).isEmpty();
		assertThat(description.help()).isEqualTo(option.help());
		assertThat(description.keys().size()).isEqualTo(1);

		String keyPrefix = "--";
		assertThat(description.keys().iterator().next()).isEqualTo(keyPrefix + methodParameter.getParameterName());
	}
	
	// Tests for support
	
	@Test
	public void testNotSupportedParameter() {
		Method method = findMethod(Remote.class, "nonInteractive", String.class, String.class);

		assertThat(resolver.supports(Utils.createMethodParameter(method, 0))).isFalse();
		assertThat(resolver.supports(Utils.createMethodParameter(method, 1))).isFalse();
	}
	
	@Test
	public void testNotSupportedMethod() {
		Method method = findMethod(Remote.class, "notAShellMethod", String.class);

		assertThat(resolver.supports(Utils.createMethodParameter(method, 0))).isFalse();
	}

	@Test
	public void testSupportedParameters() {
		Method method = findMethod(Remote.class, "interactive", String.class);

		assertThat(resolver.supports(Utils.createMethodParameter(method, 0))).isTrue();
	}

}
