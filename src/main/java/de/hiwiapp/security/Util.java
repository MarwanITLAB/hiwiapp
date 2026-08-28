package de.hiwiapp.security;

import org.owasp.validator.html.*;

public class Util {

	private static final String POLICY_FILE = "antisamy_ibw_dot.xml";
	private static Policy instance = null;
	private static AntiSamy antiSamy = null;



	public static String toSafeHtml(String html) throws ScanException, PolicyException {

		if(html == null) return null;
		if(instance == null) {
			java.net.URL resource = Util.class.getClassLoader().getResource(POLICY_FILE);

			instance = Policy.getInstance(resource);
		}
		if(antiSamy == null) {
			antiSamy = new AntiSamy();
		}

		CleanResults cleanResults = antiSamy.scan(html, instance);
		return cleanResults.getCleanHTML().trim();

	}

	public static boolean isSafe(String html) throws ScanException, PolicyException {
		return html.equals(toSafeHtml(html));
	}









}
