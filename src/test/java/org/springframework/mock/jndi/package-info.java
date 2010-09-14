/*
 * The simplest implementation of the JNDI SPI that could possibly work.
 *
 * <p>Useful for setting up a simple JNDI environment for test suites
 * or standalone applications. If e.g. JDBC DataSources get bound to the
 * same JNDI names as within a J2EE container, both application code and
 * configuration can me reused without changes.
 */

package org.springframework.mock.jndi;
