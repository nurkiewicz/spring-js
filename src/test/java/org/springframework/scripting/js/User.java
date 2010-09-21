package org.springframework.scripting.js;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-09-18, 21:26:53
 */
public class User {

	private String name;

	private Country country;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public User withCountry(Country country) {
		setCountry(country);
		return this;
	}

	public User withName(String name) {
		setName(name);
		return this;
	}
}
