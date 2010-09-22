package org.springframework.scripting.js;

import org.fest.assertions.Fail;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-09-22, 19:35:00
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class JavaScriptScriptFactoryErrorHandlingTest {

	@Resource
	private HelloService helloNotExistingVar;

	@Resource
	private HelloService helloExternalException;

	@Resource
	private HelloService helloMissingMethod;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void shouldHandleNotExistingVariable() throws Exception {
		//given

		//when
		exception.expectMessage(containsString("unknownVar"));
		helloNotExistingVar.hello("Tomek");

		//then
	}

	@Test
	public void shouldHandleExternalException() throws Exception {
		//given
		final String filename = "fooBar.6574635421453.tmp";

		//when
		exception.expectMessage(containsString(filename));
		helloExternalException.hello(filename);

		//then
	}

	@Test
	public void shouldHandleMissingInterfaceMethodImplementation() throws Exception {
		//given

		//when
		exception.expectMessage(containsString("hello"));
		helloMissingMethod.hello("Tomek");

		//then
	}
	
}
