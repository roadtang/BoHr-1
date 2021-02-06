package org.bohr.message;

public class LanguageChoiceConfig {
	public final static String TYPE_EN = "en";
	public static String type = TYPE_EN;

	public static boolean is_choose_en() {
		if (TYPE_EN.equals(type)) {
			return true;
		}
		return false;
	}
}
