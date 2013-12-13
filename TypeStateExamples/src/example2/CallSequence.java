package example2;

import java.util.ArrayList;
import java.util.List;

public class CallSequence {

	private static final int TIMEOUT = 60000;
	
	private final List<AsyncRemoteCall> calls = new ArrayList<AsyncRemoteCall>();
	
	public void append(final AsyncRemoteCall call) {
		calls.add(call);
	}
	
	public void execute() {
		for (AsyncRemoteCall call : calls) {
			call.execute();
			
			try {
				call.wait(TIMEOUT);
			} catch (InterruptedException e) {
				// fail
				break;
			}
			
			if (!call.isFinishedSuccessfully()) {
				// fail
				break;
			}
		}
	}
}
