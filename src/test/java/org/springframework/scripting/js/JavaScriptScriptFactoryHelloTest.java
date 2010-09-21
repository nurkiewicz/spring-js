package org.springframework.scripting.js;

import org.fest.assertions.Fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-09-18, 20:13:09
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class JavaScriptScriptFactoryHelloTest {

	@Resource
	private HelloService helloService;

	@Test
	public void shouldReturnHelloStringFromJs() throws Exception {
		//given
		final String name = "Tomek";

		//when
		final String result = helloService.hello(name);

		//then
		assertThat(result).isEqualTo("Hello, Tomek!");
	}

	@Test
	public void shouldNotThrowWhenNullName() throws Exception {
		//given
		final String name = null;

		//when
		final String result = helloService.hello(name);

		//then
		assertThat(result).isEqualTo("Hello, null!");
	}

	@Test
	public void shouldHandleBuiltInJAvaTypes() throws Exception {
		//given
		final Date effectiveDate = new GregorianCalendar(2010, Calendar.SEPTEMBER, 18, 21, 41, 50).getTime();

		//when
		final String result = helloService.helloParameterized("Jan", effectiveDate, 25, new Locale("pl", "PL"));

		//then
		assertThat(result).isEqualTo("Sat Sep 18 21:41:50 CEST 2010: Jan (26, Poland)");
	}

}
