package org.ayou.search.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @category 压缩没问题但压不了带文件夹的文件
 */
public class ZipUtils {

	public static void doCompress(String srcFile, String zipFile) throws Exception {
		doCompress(new File(srcFile), new File(zipFile));
	}

	/**
	 * 文件压缩
	 * @param srcFile 目录或者单个文件
	 * @param destFile 压缩后的ZIP文件
	 */
	public static void doCompress(File srcFile, File destFile) throws Exception {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destFile));
		if (srcFile.isDirectory()) {
			File[] files = srcFile.listFiles();
			for (File file : files) {
				doCompress(file, out);
			}
		} else {
			doCompress(srcFile, out);
		}
	}

	/**
	 * 文件压缩
	 * @param list  压缩一个list的文件
	 * @param destFile 压缩后的ZIP文件
	 */
	public static void doCompress(List<String> list, File destFile) throws Exception {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destFile));
		for (String string : list) {
			doCompress(new File(string), out);
		}
	}

	public static void doCompress(String pathname, ZipOutputStream out) throws IOException {
		doCompress(new File(pathname), out);
	}

	public static void doCompress(File file, ZipOutputStream out) throws IOException {
		if (file.exists()) {
			byte[] buffer = new byte[1024];
			RandomAccessFile fis = new RandomAccessFile(file, "r");
			out.putNextEntry(new ZipEntry(file.getName()));
			int len = 0;
			// 读取文件的内容,打包到zip文件
			while ((len = fis.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}
			out.flush();
			out.closeEntry();
			fis.close();
		}
	}

	public static void main(String[] args) {
		try {
			doCompress(new File("D://Interface/reNewalQuery/20170731"), new File("D:\\Interface\\reNewalQuery\\20170731.zip"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
