import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import annotation.DefinitelyVulnerable;


public class Main {

	@DefinitelyVulnerable
	public static void main(String[] args) {
		Collection<String> l = new LinkedList<String>();
		Iterator<String> iterator = l.iterator();
		l.add("");
		iterator.next();
	}

}
