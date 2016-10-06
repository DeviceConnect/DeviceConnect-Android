/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import com.barchart.udt.net.NetServerSocketUDT;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class AppServer {


	public static void main(final String[] args) throws IOException {

		int port = 9000;

		if (args.length > 1) {
			System.out.println("usage: appserver [server_port]");
			return;
		}

		if (args.length == 1) {
			port = Integer.parseInt(args[0]);
		}

		final NetServerSocketUDT acceptorSocket = new NetServerSocketUDT();
		acceptorSocket.bind(new InetSocketAddress("0.0.0.0", port), 256);

		System.out.printf("server is ready at port: %d\n", port);

		while (true) {

			final Socket clientSocket = acceptorSocket.accept();

			// Start the read ahead background task
			Executors.newSingleThreadExecutor().submit(new Callable<Boolean>() {
				@Override
				public Boolean call() {
					return clientTask(clientSocket);
				}
			});
		}
	}

	public static boolean clientTask(final Socket clientSocket) {

		final byte[] data = new byte[10000];

		try {

			final InputStream is = clientSocket.getInputStream();

			while (true) {

				int remain = data.length;

				while (remain > 0) {
					final int ret = is.read(data, data.length - remain, remain);
					remain -= ret;
				}
			}

		} catch (final IOException ioe) {
			ioe.printStackTrace();
			return false;
		}
	}
}
