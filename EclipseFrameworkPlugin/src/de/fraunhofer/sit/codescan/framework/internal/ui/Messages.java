package de.fraunhofer.sit.codescan.framework.internal.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "de.fraunhofer.sit.codescan.framework.internal.ui.messages"; //$NON-NLS-1$
	public static String PreferencePage_IDError;
	public static String PreferencePage_IDWarning;
	public static String PreferencePage_MarkerStyle;
	public static String PreferencePage_MnemonicError;
	public static String PreferencePage_MnemonicWarning;
	public static String PreferencePage_PageDescription;
	public static String PreferencePage_RadioGroupLabel;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
