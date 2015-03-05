package com.cht.iTest.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.io.IOUtils;

public class ObjectUtils {
	
	@SuppressWarnings("unchecked")
	public static <T> T deepCopy(T obj) throws IOException, ClassNotFoundException {
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;
		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;
		T res = null;
		
		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			oos.flush();
			bais = new ByteArrayInputStream(baos.toByteArray());
			ois = new ObjectInputStream(bais);
			res = (T) ois.readObject();
		} finally {
			IOUtils.closeQuietly(ois);
			IOUtils.closeQuietly(bais);
			IOUtils.closeQuietly(oos);
			IOUtils.closeQuietly(baos);
		}
		
		return res;
	}
	
}
