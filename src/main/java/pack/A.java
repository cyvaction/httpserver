package pack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class A {

	public static void main(String[] args) throws Exception {
		System.out.println("============================================");
		ClassLoader cl = new MyClassLoader("pack-b-0.0.1-SNAPSHOT");
		Class<?> clazz = cl.loadClass("pack.B");
		System.out.println(clazz.getName());
		Object b = clazz.newInstance();
		Method m = clazz.getMethod("say");
		m.invoke(b);
		Thread.sleep(100000);
	}

	private static class MyClassLoader extends URLClassLoader {

		public MyClassLoader(String jarNameWithoutExtension) throws IOException {
			super(new URL[]{});
			InputStream jarStream = ClassLoader.getSystemClassLoader().getResourceAsStream(jarNameWithoutExtension + ".jar");

			if (jarStream == null) {
				throw new FileNotFoundException(jarNameWithoutExtension + ".jar");
			}

			File file = File.createTempFile(jarNameWithoutExtension, ".jar");
			file.deleteOnExit();

			OutputStream out = new FileOutputStream(file);
			try {
				copy(jarStream, out, 8096);
			} finally {
				out.close();
			}

			super.addURL(file.toURI().toURL());
		}

	}

	private static int copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
		try {
			byte[] buffer = new byte[bufferSize];
			int count = 0;
			int n = 0;
			while (-1 != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
				count += n;
			}
			return count;
		} finally {
			input.close();
			output.close();
		}
	}
}
