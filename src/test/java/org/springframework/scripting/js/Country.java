package org.springframework.scripting.js;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-09-18, 21:27:13
 */
public class Country {

	private String capital;

	public String getCapital() {
		return capital;
	}

	public void setCapital(String capital) {
		this.capital = capital;
	}

	public Country withCapital(String capital) {
		setCapital(capital);
		return this;
	}
}
