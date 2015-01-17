package org.ccnx.ccnx;

import java.io.*;

public class DataSerializer {

	public static Object unserializeObject(String serializabledObject) {
		try {
			byte[] data = Base64Coder.decode(serializabledObject);
			ObjectInputStream ois;
			ois = new ObjectInputStream(new ByteArrayInputStream(data));
			Object o = ois.readObject();
			ois.close();
			return o;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static String serializeObject(Serializable toSerialize)
			throws IOException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(toSerialize);
			oos.close();
			return new String(Base64Coder.encode(baos.toByteArray()));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}
}