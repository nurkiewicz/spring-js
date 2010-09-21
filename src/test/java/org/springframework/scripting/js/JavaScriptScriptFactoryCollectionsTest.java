package org.springframework.scripting.js;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-09-21, 19:40:16
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class JavaScriptScriptFactoryCollectionsTest {

	@Resource
	private UserService userService;

	@Test
	public void shouldHandleSetsAndMaps() throws Exception {
		//given
		Map<Integer, User> map = new HashMap<Integer, User>();
		final User user1 = new User()
				.withName("Jan")
				.withCountry(
						new Country()
								.withCapital("Warsaw"));
		final User user2 = new User()
				.withName("John")
				.withCountry(
						new Country()
								.withCapital("London"));
		map.put(3, user1);
		map.put(7, user2);

		//when
		final Set<Integer> result = userService.validate(map);

		//then
		assertThat(result).containsOnly(3, 7);
	}

	@Test
	public void shouldDumpUserWithCountryWithoutCapital() throws Exception {
		//given
		final User user = new User()
				.withName("Tomek")
				.withCountry(
						new Country()
				);

		//when
		final String result = userService.dump(user);

		//then
		assertThat(result).isEqualTo("User[name=Tomek, country=Country[capital=null]]");
	}

	@Test
	public void shouldDumpFullUser() throws Exception {
		//given
		final User user = new User()
				.withName("Martha")
				.withCountry(
						new Country()
								.withCapital("Rome")
				);

		//when
		final String result = userService.dump(user);

		//then
		assertThat(result).isEqualTo("User[name=Martha, country=Country[capital=Rome]]");
	}

	@Test
	public void shouldHandleArraysAndLists() throws Exception {
		//given

		//when
		final List<User> list = userService.loadByIds(new int[]{4, 6, 10});

		//then
		assertThat(list).hasSize(3);
	}

}
