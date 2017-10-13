package org.ayou.search.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FileUtil {
	static final ByteBuffer bufferTmp = ByteBuffer.allocate(0x4000);// 16k
	/**
	 * 读取文件
	 * @param file
	 * @return
	 */
	public static String readerFile(String file) {
		File fileByte = new File(file);
		StringBuffer sb = new StringBuffer();
		FileInputStream out = null;
		InputStreamReader isr = null;
		try {
			out = new FileInputStream(fileByte);
			isr = new InputStreamReader(out, codeString(file));
			int ch = 0;
			while ((ch = isr.read()) != -1) {
				sb.append((char) ch);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
				isr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * 判断文件的编码格式
	 * @param fileName
	 * @return
	 */
	public static String codeString(String fileName) {
		BufferedInputStream bin = null;
		try {
			bin = new BufferedInputStream(new FileInputStream(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		int p = 0;
		try {
			p = (bin.read() << 8) + bin.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String code = null;
		switch (p) {
			case 0xefbb :
				code = "UTF-8";
				break;
			case 0xfffe :
				code = "Unicode";
				break;
			case 0xfeff :
				code = "UTF-16BE";
				break;
			default :
				code = "GBK";
		}
		return code;
	}

	/**
	 * 判断文件编码格式 只读前3个字节
	 * @param file
	 * @return
	 */
	public static String codeString2(String file) {
		RandomAccessFile randomAccessFile = null;
		FileChannel channel = null;
		ByteBuffer buffer = ByteBuffer.allocate(3);
		String code = "UTF-8";//default utf-8
		try {
			randomAccessFile = new RandomAccessFile(file, "r");
			channel = randomAccessFile.getChannel();
			channel.read(buffer);
			code = codeString(buffer.array());
			buffer.clear();
			buffer = null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (channel != null)channel.close();
				if (randomAccessFile != null)randomAccessFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return code;
	}

	/**
	 * 判断文件编码
	 * @param file
	 * @return
	 */
	public static String codeString(byte... bytes) {
		int b1 = bytes[0];
		int b2 = bytes[1];
		int b3 = bytes[2];
		if (b1 == -17 && b2 == -69 && b3 == -65) {
			return "UTF-8";
		} else if (b1 == 60 && b2 == 33 && b3 == 45) {
			return "GBK";
		} else if (b1 == 92 && b2 == 117 && b3 == 70) {// Unicode
			return "UTF-8";// 这里是被编码过的要转码
		} else if (b1 == 104 && b2 == 101 && b3 == 108) {// ASCII
			return "GBK";// 返GBK才能正确读取
		}
		return "UTF-8";
	}

	/**
	 * NIO读取文件
	 * @param file文件路径
	 * @param tmp 缓冲区1024的倍数
	 * @return string
	 */
	public static byte[] readBytesByNIO(String file, int tmp) {
		RandomAccessFile randomAccessFile = null;
		FileChannel channel = null;
		try {
			randomAccessFile = new RandomAccessFile(file, "r");
			// 第一步 获取通道
			channel = randomAccessFile.getChannel();
			// 文件内容的大小
			int size = (int) channel.size();
			// 第二步 指定缓冲区
			ByteBuffer buffer = ByteBuffer.allocate(1024 * tmp);
			// 第三步 将通道中的数据读取到缓冲区中
			// channel.read(buffer);
			byte[] bt = new byte[size];
			int x = 0;
			while (channel.read(buffer) > 0) {
				int pos = buffer.position();
				buffer.rewind();// 将position设回0
				// buffer.get(pos);
				System.arraycopy(buffer.array(), 0, bt, x, pos);
				x += pos;
			}
			// Buffer bf = buffer.flip();
			buffer.clear();
			buffer = null;
			return bt;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (channel != null)channel.close();
				if (randomAccessFile != null)randomAccessFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 读取文件返回字符串
	 * @param file
	 * @param tmp
	 * @return
	 */
	public static String readStringByNIO(String file, int tmp) {
		byte[] bt = readBytesByNIO(file, tmp);
		String str = null;
		try {
			str = new String(bt, 0, bt.length, codeString(bt));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return str;
	}
	
	/**
	 * 判断文件是否存在
	 * @param path
	 * @return
	 */
	public static boolean exists(String path) {
		if (path == null || path.equals("")) {
			return false;
		}
		try {
			Path p = Paths.get(path);
			return Files.exists(p);
			// return Files.exists(p, new LinkOption[]{LinkOption.NOFOLLOW_LINKS});
		} catch (Exception e) {
			return false;
		}
	}

	public static String read(String path, int tmp) {
		ByteBuffer buffer = ByteBuffer.allocate(tmp);
		AsynchronousFileChannel fileChannel = null;
		String str = null;
		try {
			Path p = Paths.get(path);
			fileChannel = AsynchronousFileChannel.open(p, StandardOpenOption.READ);
			int size = (int) fileChannel.size();
			int position = 0;
			byte[] bt = new byte[size];
			while (position < size) {
				Future<Integer> operation = fileChannel.read(buffer, position);
				if (operation.get() > 0) {
					int pos = buffer.position();
					buffer.rewind();
					System.arraycopy(buffer.array(), 0, bt, position, pos);
					position += buffer.limit();
				}
			}
			// buffer.flip();
			str = new String(bt, 0, size, codeString(bt));
			buffer.clear();
			buffer = null;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileChannel != null)
					fileChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return str;
	}

	public static void main(String[] args) {
		// System.out.println(readByNIO("D:\\Interface\\makePremium\\20170807\\0ac5cb4f867f45378098019c49f8f7ad_request.xml", 1));
		System.out.println(read("D:\\Interface\\makePremium\\20170807\\0ac5cb4f867f45378098019c49f8f7ad_request.xml",16));
	}
}
