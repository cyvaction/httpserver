package org.github.httpserver.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IoUtils {
	/**
	 * 拷贝流，使用默认Buffer大小
	 * @param in 输入流
	 * @param out 输出流
	 * @throws IOException
	 */
	public static int copy(InputStream in, OutputStream out) throws IOException {
		return copy(in, out, 1024);
	}
	
	/**
	 * 拷贝流
	 * @param in 输入流
	 * @param out 输出流
	 * @param bufferSize 缓存大小
	 * @throws IOException
	 */
	public static int copy(InputStream in, OutputStream out, int bufferSize) throws IOException {
		byte[] buffer = new byte[bufferSize];
		int count = 0;
		for (int n = -1; (n = in.read(buffer)) != -1;) {
			out.write(buffer, 0, n);
			count += n;
		}
		out.flush();
		return count;
	}
}
