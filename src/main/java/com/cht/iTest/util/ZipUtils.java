package com.cht.iTest.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.IOUtils;

/**
 * 
 * 壓縮檔案ZIP工具類別
 * 
 * @author wen
 *
 */
public class ZipUtils {

	/**
	 * 
	 * 壓縮檔案為ZIP, 並輸送至OutputStream
	 * 
	 * @param file
	 * @param os
	 * @throws Exception
	 */
	public static void compress(File file, OutputStream os) throws Exception {
		ArchiveOutputStream aos = null;

		try {
			aos = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, os);
			recursiveScan(file, aos);
			aos.finish();
		} finally {
			IOUtils.closeQuietly(aos);
		}
	}

	/**
	 * 
	 * 將檔案轉換為ZIP後，進行byte[]輸出
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static byte[] compress2ByteAry(File file) throws Exception {
		ByteArrayOutputStream baos = null;
		byte[] res = null;

		try {
			baos = new ByteArrayOutputStream();
			compress(file, baos);
			res = baos.toByteArray();
		} finally {
			IOUtils.closeQuietly(baos);
		}

		return res;
	}

	private static void recursiveScan(File file, ArchiveOutputStream aos) throws IOException {
		if (file.isFile()) {
			FileInputStream fis = null;

			try {
				fis = new FileInputStream(file);
				aos.putArchiveEntry(new ZipArchiveEntry(file.getParentFile().getName() + File.separatorChar + file.getName()));
				IOUtils.copy(fis, aos);
				aos.closeArchiveEntry();
			} finally {
				IOUtils.closeQuietly(fis);
			}
		} else {
			File[] ary = file.listFiles();

			if (ary != null) {
				for (File temp : ary) {
					recursiveScan(temp, aos);
				}
			}
		}
	}

}
