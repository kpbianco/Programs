package edu.nmsu.cs.webserver;

/**
 * Web worker: an object of this class executes in its own new thread to receive and respond to a
 * single HTTP request. After the constructor the object executes on its "run" method, and leaves
 * when it is done.
 *
 * One WebWorker object is only responsible for one client connection. This code uses Java threads
 * to parallelize the handling of clients: each WebWorker runs in its own thread. This means that
 * you can essentially just think about what is happening on one client at a time, ignoring the fact
 * that the entirety of the webserver execution might be handling other clients, too.
 *
 * This WebWorker class (i.e., an object of this class) is where all the client interaction is done.
 * The "run()" method is the beginning -- think of it as the "main()" for a client interaction. It
 * does three things in a row, invoking three methods in this class: it reads the incoming HTTP
 * request; it writes out an HTTP header to begin its response, and then it writes out some HTML
 * content for the response content. HTTP requests and responses are just lines of text (in a very
 * particular format).
 * 
 * @author Jon Cook, Ph.D.
 *
 **/

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.io.FileReader;
import java.io.FileInputStream;
import java.util.Scanner;
import java.io.BufferedInputStream;
import java.time.*;

public class WebWorker implements Runnable
{

	private Socket socket;

	/**
	 * Constructor: must have a valid open socket
	 **/
	public WebWorker(Socket s)
	{
		socket = s;
	}

	/**
	 * Worker thread starting point. Each worker handles just one HTTP request and then returns, which
	 * destroys the thread. This method assumes that whoever created the worker created it with a
	 * valid open socket object.
	 **/
	public void run()
	{
		System.err.println("Handling connection...");
		try
		{
			String filename;
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			filename = readHTTPRequest(is);
			
			if(filename == " / ") {
				writeHTTPHeader(os, "text/html", filename);
				htmlWriter(os, filename);
			}

			if(filename.contains(".html")) {
				writeHTTPHeader(os, "text/html", filename);
				htmlWriter(os, filename);
			}

			writeContent(os);
			os.flush();
			socket.close();
		}
		catch (Exception e)
		{
			System.err.println("Output error: " + e);
		}
		System.err.println("Done handling connection.");
		return;
	}

	/**
	 * Read the HTTP request header.
	 **/
	private String readHTTPRequest(InputStream is)
	{
		String line;
		String file = "";
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
		while (true)
		{
			try
			{
				while (!r.ready())
					Thread.sleep(1);
				line = r.readLine();
				System.err.println("Request line: (" + line + ")");
				if(line.startsWith("GET")) {
					file = line.substring(5, line.length()-9);
						
				}
				if (line.length() == 0)
					break;
			}
			catch (Exception e)
			{
				System.err.println("Request error: " + e);
				break;
			}
		}
		return file;
	}

	public void htmlWriter(OutputStream os, String filename) {
		try {
			FileReader file = new FileReader(filename);
			Scanner scanner = new Scanner(file);
			String line = "";
			while(scanner.hasNextLine()) {
				line = scanner.nextLine();
				LocalDate currentTime = LocalDate.now();
				line = line.replaceAll("<cs371date>", currentTime.toString());
				line = line.replaceAll("<cs371server>", "Kian Bianco's 371 Server");
				os.write(line.getBytes());
			}
			scanner.close();
			}catch(Exception e){
				System.err.println("Output error: " + e);
			}
		return;
	}

	/**
	 * Write the HTTP header lines to the client network connection.
	 * 
	 * @param os
	 *          is the OutputStream object to write to
	 * @param contentType
	 *          is the string MIME content type (e.g. "text/html")
	 **/
	private void writeHTTPHeader(OutputStream os, String contentType, String file) throws Exception
	{
		int flag = 0;
		Date d = new Date();
		DateFormat df = DateFormat.getDateTimeInstance();
		df.setTimeZone(TimeZone.getTimeZone("GMT"));

		try {
			FileReader filename = new FileReader(file);
		}catch(Exception e) {
			os.write("HTTP/1.0 404 Not Found\n".getBytes());
			flag = 1;
		}
		finally {
			if(flag == 0) {
				os.write("HTTP/1.1 200 OK\n".getBytes());
			}
		}

		os.write("Date: ".getBytes());
		os.write((df.format(d)).getBytes());
		os.write("\n".getBytes());
		os.write("Server: Kian's very own server\n".getBytes());
		os.write("Connection: close\n".getBytes());
		os.write("Content-Type: ".getBytes());
		os.write(contentType.getBytes());
		os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
		return;
	}

	/**
	 * Write the data content to the client network connection. This MUST be done after the HTTP
	 * header has been written out.
	 * 
	 * @param os
	 *          is the OutputStream object to write to
	 **/
	private void writeContent(OutputStream os) throws Exception
	{
		os.write("<html><head></head><body>\n".getBytes());
		os.write("<h3>My web server works!</h3>\n".getBytes());
		os.write("</body></html>\n".getBytes());
	}

} // end class
