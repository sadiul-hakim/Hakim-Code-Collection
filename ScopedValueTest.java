public class ScopedValueTest {
	
	public static final ScopedValue<String> SCOPED_VALUE = ScopedValue.newInstance();
	
	public static void main(String[] args) {
		
		if(SCOPED_VALUE.isBound()) {
			System.out.println("Beginning of the Main method : "+SCOPED_VALUE.get());
		}
		
		// Scoped Value is Immutable.
		// Value bound to ScopedValue is not only associated with that very Thread also that very scope.
		try {
			ScopedValue.callWhere(SCOPED_VALUE, "New Call", ()->{
				
				if(SCOPED_VALUE.isBound()) {
					System.out.println("Beginning of the  Scope : "+SCOPED_VALUE.get());
				}
				
				return "";
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(SCOPED_VALUE.isBound()) {
			System.out.println("End of the Main method : "+SCOPED_VALUE.get());
		}
	}
}
