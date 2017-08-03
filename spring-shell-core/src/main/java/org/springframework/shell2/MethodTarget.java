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

package org.springframework.shell2;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Represents a shell command behavior, <em>i.e.</em> code to be executed when a command is requested.
 *
 * @author Eric Bottard
 */
public class MethodTarget {

	private final Method method;

	private final Object bean;

	private final String help;

	public MethodTarget(Method method, Object bean, String help) {
		Assert.notNull(method, "Method cannot be null");
		Assert.notNull(bean, "Bean cannot be null");
		Assert.hasText(help, "Help cannot be blank");
		ReflectionUtils.makeAccessible(method);
		this.method = method;
		this.bean = bean;
		this.help = help;
	}

	/**
	 * Construct a MethodTarget for the unique method named {@literal name} on the given object. Fails with an exception
	 * in case of overloaded method.
	 */
	public static MethodTarget of(String name, Object bean, String help) {
		Set<Method> found = new HashSet<>();
		ReflectionUtils.doWithMethods(bean.getClass(), found::add, m -> m.getName().equals(name));
		if (found.size() != 1) {
			throw new IllegalArgumentException(String.format("Could not find unique method named '%s' on object of class %s. Found %s",
				name, bean.getClass(), found));
		}
		return new MethodTarget(found.iterator().next(), bean, help);
	}

	public Method getMethod() {
		return method;
	}

	public Object getBean() {
		return bean;
	}

	public String getHelp() {
		return help;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MethodTarget that = (MethodTarget) o;

		if (!method.equals(that.method)) return false;
		if (!bean.equals(that.bean)) return false;
		return help.equals(that.help);

	}

	@Override
	public int hashCode() {
		int result = method.hashCode();
		result = 31 * result + bean.hashCode();
		result = 31 * result + help.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return method.toString();
	}
}
