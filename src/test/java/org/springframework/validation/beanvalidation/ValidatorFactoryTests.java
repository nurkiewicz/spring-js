/*
 * Copyright 2002-2010 the original author or authors.
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

package org.springframework.validation.beanvalidation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.HibernateValidator;
import static org.junit.Assert.*;
import org.junit.Test;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/**
 * @author Juergen Hoeller
 * @since 3.0
 */
public class ValidatorFactoryTests {

	@Test
	public void testSimpleValidation() throws Exception {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		ValidPerson person = new ValidPerson();
		Set<ConstraintViolation<ValidPerson>> result = validator.validate(person);
		assertEquals(2, result.size());
		for (ConstraintViolation<ValidPerson> cv : result) {
			String path = cv.getPropertyPath().toString();
			if ("name".equals(path) || "address.street".equals(path)) {
				assertTrue(cv.getConstraintDescriptor().getAnnotation() instanceof NotNull);
			}
			else {
				fail("Invalid constraint violation with path '" + path + "'");
			}
		}
	}

	@Test
	public void testSimpleValidationWithCustomProvider() throws Exception {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.setProviderClass(HibernateValidator.class);
		validator.afterPropertiesSet();
		ValidPerson person = new ValidPerson();
		Set<ConstraintViolation<ValidPerson>> result = validator.validate(person);
		assertEquals(2, result.size());
		for (ConstraintViolation<ValidPerson> cv : result) {
			String path = cv.getPropertyPath().toString();
			if ("name".equals(path) || "address.street".equals(path)) {
				assertTrue(cv.getConstraintDescriptor().getAnnotation() instanceof NotNull);
			}
			else {
				fail("Invalid constraint violation with path '" + path + "'");
			}
		}
	}

	@Test
	public void testSimpleValidationWithClassLevel() throws Exception {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		ValidPerson person = new ValidPerson();
		person.setName("Juergen");
		person.getAddress().setStreet("Juergen's Street");
		Set<ConstraintViolation<ValidPerson>> result = validator.validate(person);
		assertEquals(1, result.size());
		Iterator<ConstraintViolation<ValidPerson>> iterator = result.iterator();
		ConstraintViolation cv = iterator.next();
		assertEquals("", cv.getPropertyPath().toString());
		assertTrue(cv.getConstraintDescriptor().getAnnotation() instanceof NameAddressValid);
	}

	@Test
	public void testSpringValidation() throws Exception {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		ValidPerson person = new ValidPerson();
		BeanPropertyBindingResult result = new BeanPropertyBindingResult(person, "person");
		validator.validate(person, result);
		assertEquals(2, result.getErrorCount());
		FieldError fieldError = result.getFieldError("name");
		assertEquals("name", fieldError.getField());
		System.out.println(Arrays.asList(fieldError.getCodes()));
		System.out.println(fieldError.getDefaultMessage());
		fieldError = result.getFieldError("address.street");
		assertEquals("address.street", fieldError.getField());
		System.out.println(Arrays.asList(fieldError.getCodes()));
		System.out.println(fieldError.getDefaultMessage());
	}

	@Test
	public void testSpringValidationWithClassLevel() throws Exception {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		ValidPerson person = new ValidPerson();
		person.setName("Juergen");
		person.getAddress().setStreet("Juergen's Street");
		BeanPropertyBindingResult result = new BeanPropertyBindingResult(person, "person");
		validator.validate(person, result);
		assertEquals(1, result.getErrorCount());
		ObjectError fieldError = result.getGlobalError();
		System.out.println(Arrays.asList(fieldError.getCodes()));
		System.out.println(fieldError.getDefaultMessage());
	}


	@NameAddressValid
	public static class ValidPerson {

		@NotNull
		private String name;

		@Valid
		private ValidAddress address = new ValidAddress();

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public ValidAddress getAddress() {
			return address;
		}

		public void setAddress(ValidAddress address) {
			this.address = address;
		}
	}


	public static class ValidAddress {

		@NotNull
		private String street;

		public String getStreet() {
			return street;
		}

		public void setStreet(String street) {
			this.street = street;
		}
	}


	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = NameAddressValidator.class)
	public @interface NameAddressValid {

		String message() default "Street must not contain name";

		Class<?>[] groups() default {};

		Class<?>[] payload() default {};
	}


	public static class NameAddressValidator implements ConstraintValidator<NameAddressValid, ValidPerson> {

		public void initialize(NameAddressValid constraintAnnotation) {
		}

		public boolean isValid(ValidPerson value, ConstraintValidatorContext constraintValidatorContext) {
			return (value.name == null || !value.address.street.contains(value.name));
		}
	}

}
