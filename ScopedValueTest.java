import java.util.concurrent.StructuredTaskScope;

public class ScopedValueTest {

	public static final ScopedValue<String> SCOPED_VALUE = ScopedValue.newInstance();

	public static void main(String[] args) {

		if (SCOPED_VALUE.isBound()) {
			System.out.println(Thread.currentThread() + " Beginning of the Main method : " + SCOPED_VALUE.get());
		}

		// Scoped Value is Immutable.
		// Value bound to ScopedValue is not only associated with that very Thread also
		// that very scope.
		// The moment the method completes, The ScopedValue value is unbound and it will
		// be cleared up automatically.
		try {
			ScopedValue.callWhere(SCOPED_VALUE, "New Call", () -> {

				Thread.ofVirtual().name("New Thread::").start(() -> {
					
					// isBound is false as this code is inside a new thread
					if (SCOPED_VALUE.isBound()) {
						System.out.println(Thread.currentThread() + " Beginning of the  Scope under New Thread : " + SCOPED_VALUE.get());
					}
				});

				/*
    					Even Though the new Thread ("New Thread::" - Line 21) declared under same scope the string associated with Scoped Value seems not available under "New Thread::".
	 But the value associated with Scoped Value is available under the threads we create using StructuredTaskScope. You can run the code and see the result.
    				*/
				
				try(var scope = new StructuredTaskScope<String>()){
					scope.fork(()->{
						
						if(SCOPED_VALUE.isBound()) {							
							System.out.println(Thread.currentThread() + " under StructuredTaskScope : " + SCOPED_VALUE.get());
						}
						return "Done";
					});
					
					scope.join();
				}
				
				if (SCOPED_VALUE.isBound()) {
					System.out.println(Thread.currentThread() + " Beginning of the  Scope : " + SCOPED_VALUE.get());
				}

				return "";
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// isBound is false as this code is outside of the dynamic scope
		if (SCOPED_VALUE.isBound()) {
			System.out.println(Thread.currentThread() + " End of the Main method : " + SCOPED_VALUE.get());
		}
	}
}
