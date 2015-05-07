import java.security.MessageDigest;

import org.eclipse.jetty.util.ArrayUtil;

public class Hasher {
	static String salt;
	public static void setSalt(String s){
		salt = s;
	}
	public static String Hash(String string){
		try{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			String value = string+salt;
			md.update(value.getBytes("UTF-8"));
			byte[] hash = md.digest();
			StringBuffer hexString = new StringBuffer();

		        for (int i = 0; i < hash.length; i++) {
		            String hex = Integer.toHexString(0xff & hash[i]);
		            if(hex.length() == 1) hexString.append('0');
		            hexString.append(hex);
		        }

		        return hexString.toString();
		}catch(Exception e){
			return null;
		}
	}
}
