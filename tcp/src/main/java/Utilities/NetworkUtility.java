package Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NetworkUtility {

	// https://mkyong.com/java/java-convert-byte-to-int-and-vice-versa/
	public static byte[] intToBytes(final int data) {
		return new byte[]{(byte) ((data >> 24) & 0xff), (byte) ((data >> 16) & 0xff), (byte) ((data >> 8) & 0xff), (byte) ((data >> 0) & 0xff),};
	}

	// https://mkyong.com/java/java-convert-byte-to-int-and-vice-versa/
	public static int bytesToInt(byte[] bytes) {
		return ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | ((bytes[3] & 0xFF) << 0);
	}

	public static void Send(OutputStream out, byte... bytes) throws IOException {
		out.write(intToBytes(bytes.length));
		out.write(bytes);
		out.flush();
	}

	// read the bytes on the stream
	// read the bytes on the stream
	private static byte[] Read(InputStream in, int length) throws IOException {
		byte[] bytes = new byte[length];
		System.out.println("Read in bytes: " + length);
		int bytesRead = 0;
		try {
			bytesRead = in.read(bytes, 0, length);
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
		if (bytesRead < length && bytesRead > 0) {
			byte[] newBytes = Read(in, length - bytesRead);
			System.arraycopy(newBytes, 0, bytes, bytesRead, newBytes.length);
		}
		return bytes;
	}

	public static byte[] Receive(InputStream in) throws IOException {
		byte[] lengthBytes = Read(in, 4);
		if (lengthBytes == null) return new byte[0];
		int length = NetworkUtility.bytesToInt(lengthBytes);
		byte[] message = Read(in, length);
		if (message == null) return new byte[0];
		return message;
	}
}
