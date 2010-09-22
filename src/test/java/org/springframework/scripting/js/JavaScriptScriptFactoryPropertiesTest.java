package org.springframework.scripting.js;

import org.fest.assertions.Fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-09-21, 22:17:29
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class JavaScriptScriptFactoryPropertiesTest {

	@Resource
	private HelloService helloService;

	@Test
	public void shouldInjectDependenciesToScriptContext() throws Exception {
		//given
		final String name = "Tomek";

		//when
		final String result = helloService.hello(name);

		//then
		assertThat(result).isEqualTo("Hello from Warsaw, " + name + "!");
	}

	
}
