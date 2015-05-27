package com.cht.iTest.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

public class JAXBUtils {

	public static <T> T xml2Bean(Class<T> clazz, URL url) throws Exception {
		JAXBContext context = JAXBContext.newInstance(clazz);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		return clazz.cast(unmarshaller.unmarshal(url));
	}

	public static <T> T xml2Bean(Class<T> clazz, String xml) throws Exception {
		JAXBContext context = JAXBContext.newInstance(clazz);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		StringReader sr = new StringReader(xml);
		return clazz.cast(unmarshaller.unmarshal(sr));
	}

	public static <T> void logBean(T jaxbObj) {
		try {
			System.out.println(bean2Xml(jaxbObj));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static <T> T xml2Bean(Class<T> clazz, InputStream is) throws Exception {
		JAXBContext context = JAXBContext.newInstance(clazz);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		return clazz.cast(unmarshaller.unmarshal(is));
	}

	public static String bean2Xml(Object jaxbElement) throws Exception {
		JAXBContext context = JAXBContext.newInstance(jaxbElement.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter sw = new StringWriter();
		marshaller.marshal(jaxbElement, sw);
		return sw.toString();
	}

	public static void bean2XmlFile(Object jaxbElement, File file) throws Exception {
		JAXBContext context = JAXBContext.newInstance(jaxbElement.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(jaxbElement, file);
	}

	public static void XSDGenerate(String outputPath, Class<?>... clazz) throws Exception {
		JAXBContext context = JAXBContext.newInstance(clazz);
		SchemaOutputResolver xsd = new SchemaOutputResolver() {
			@Override
			public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
				File file = new File(outputPath);
				StreamResult result = new StreamResult(file);
				result.setSystemId(file.toURI().toURL().toString());
				return result;
			}
		};

		context.generateSchema(xsd);
	}

}
