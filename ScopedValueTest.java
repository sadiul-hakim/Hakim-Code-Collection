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

				Thread.ofPlatform().name("New Thread::").start(() -> {
					
					// isBound is false as this code is inside a new thread
					if (SCOPED_VALUE.isBound()) {
						System.out.println(Thread.currentThread() + " Beginning of the  Scope under New Thread : " + SCOPED_VALUE.get());
					}
				});
				
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
