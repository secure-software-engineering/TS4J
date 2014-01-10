import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;


public class Main {
	
	public static void main(String[] args) {
		Collection<String> l = new LinkedList<String>();
		Iterator<String> iterator = l.iterator();
		l.add("");
		iterator.next();
	}

}
