package Utilities;

import org.json.JSONObject;

/**
 * JsonUtility.java
 *
 * @author ASU
 * updated  11/3/2021
 */
public class JsonUtility {

	public static JSONObject fromByteArray(byte[] bytes) {
		String jsonString = new String(bytes);
		return new JSONObject(jsonString);
	}

	public static byte[] toByteArray(JSONObject object) {
		return object.toString().getBytes();
	}


}
