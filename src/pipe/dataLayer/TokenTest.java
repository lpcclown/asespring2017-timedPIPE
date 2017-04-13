package pipe.dataLayer;

import static org.junit.Assert.assertEquals;

import java.util.Vector;

import org.junit.Test;

public class TokenTest {

	@Test
	public final void testCompareTo() {
		String[] types = { "string", "arrivingTime" };
		try {
			Vector<DataType> group = null;
			DataType dt = new DataType("abc", types, true, group);
			Token token = new Token(dt);
			token.definetype(dt);
			token.defineTlist(dt);
			token.Tlist.elementAt(1).setValue(2);
			Token testToken = new Token();
			testToken.definetype(dt);
			testToken.defineTlist(dt);
			testToken.Tlist.elementAt(1).setValue(1);
			int result = token.compareTo(testToken);
			assertEquals(1, result);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

}
