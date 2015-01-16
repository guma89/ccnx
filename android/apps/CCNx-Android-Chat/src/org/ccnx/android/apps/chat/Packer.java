package org.ccnx.android.apps.chat;

import java.io.*;

public class Packer {

	public byte[] serialize(Object o) {
		byte[] b = new byte[0];
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream so = new ObjectOutputStream(bo);
			so.writeObject(o);
			so.flush();
			b = bo.toByteArray();
		} catch (IOException e) {
			System.out.println("Packer.serialize -> IOException: " + e);
		}
		return b;
	}

	public Object deserialize(byte[] b) {
		Object o = new Object();
		try {
			ByteArrayInputStream bi = new ByteArrayInputStream(b);
			ObjectInputStream si = new ObjectInputStream(bi);
			o = (Object) si.readObject();
		} catch (ClassNotFoundException e) {
			System.out.println("Packer.deserialize -> ClassNotFoundException: " + e);
		} catch (IOException e) {
			System.out.println("Packer.deserialize -> IOException: " + e);
		}
		return o;
	}

	public static void main(String[] args) {
		DataStruct before = new DataStruct("Billy", "Cooper", 27);
		Packer packer = new Packer();
		byte[] data = packer.serialize(before);
		DataStruct after = (DataStruct) packer.deserialize(data);
		
		System.out.println("before    : " + before.toString());
		System.out.println("serialized: " + data.toString());
		System.out.println("after     : " + after.toString());
	}

}
