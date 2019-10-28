package com.webuildsystems.chazzofalf.bytePasserServer.ByteTosserServer.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.webuildsystems.chazzofalf.bytePasserServer.ByteTosserServer.BufferedPipe;
import com.webuildsystems.chazzofalf.bytePasserServer.ByteTosserServer.ByteTosserServer;
import com.webuildsystems.chazzofalf.bytePasserServer.ByteTosserServer.HolderInputStream;
import com.webuildsystems.chazzofalf.bytePasserServer.ByteTosserServer.MultiOutputStream;

public class ByteTosserHttpServer
{
	private ServerSocket httpServerSocket;
    private int httpPort = -1;
    private BufferedPipe pipe;
    private MultiOutputStream outs;
    private HolderInputStream holder;
    private boolean runServer = false;
    private Runnable httpServerRunnable = new Runnable(){

        @Override
        public void run() {
        	ByteTosserHttpServer.this.httpServerRunnableTask();
        }
    };
	private boolean allowHttp;

    private MultiOutputStream getOuts() {
        if (this.outs == null) {
            this.outs = new MultiOutputStream();
        }
        return this.outs;
    }

    private HolderInputStream getHolder() {
        if (this.holder == null) {
            this.holder = new HolderInputStream();
        }
        return this.holder;
    }

    private BufferedPipe getPipe() {
        if (this.pipe == null) {
            this.pipe = new BufferedPipe();
            this.pipe.setInput(this.getHolder());
            this.pipe.setOutput(this.getOuts());
           
//            this.pipe.setHandler(this); For this server if the input pipe dies we just wait for the input to be restablished.
        }
        return this.pipe;
    }

    public void setHttpPort(int port)
    {
    	this.httpPort = port;
    }

    
    private void httpServerRunnableTask()
    {
    	try
    	{
    		this.httpServerSocket = new ServerSocket(this.httpPort);
    		while (true)
    		{
    			acceptSocketAndProcessInBackground(this.httpServerSocket);
    		}
    	}
    	catch (IOException e)
    	{
    		e.printStackTrace();
    	}
    }
    
    private void acceptSocketAndProcessInBackground(ServerSocket serverSocket) {
		try
		{
			Socket soc = serverSocket.accept();
			processSocketInBackground(soc);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}

	private void processSocketInBackground(Socket soc) {
		Thread daemon = new Thread(new Runnable() {
			public void run()
			{
				try {
					processSocket(soc);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		daemon.setDaemon(true);
		daemon.start();
		
	}

	protected void processSocket(Socket soc) throws IOException {
		// TODO Auto-generated method stub
		handleSocket(soc);
	}

	

    
    
	protected void handleOutputSocket(Socket outputSocket) throws IOException {
		this.getOuts().addOutputStream(outputSocket.getOutputStream());
	}

    public void start() {
    	
    		this.runServer = true;
            this.getPipe().start();
            Thread http = new Thread(this.httpServerRunnable);
            http.start();
    	
    }

    public void stop() {
        this.getPipe().stop();
    }

    

	public void setAllowHttp(boolean allowHttp) {
		this.allowHttp = allowHttp;
		
	}
	
	
	
	protected void handleSocket(Socket outputSocket) throws IOException {
		// TODO Auto-generated method stub
		String requestHeader = getRequestHeader(outputSocket);
		String path = getRequestPath(requestHeader);
		boolean inputAllowed = outputSocket.getInetAddress().isLoopbackAddress();
		handleConnectionForPath(outputSocket,path,inputAllowed);
	}
	
	private String getRequestHeader(Socket outputSocket) throws IOException
	{
		ArrayList<String> lines = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(outputSocket.getInputStream()));
		String line = null;
		while ((line = reader.readLine()) != null)
		{
			lines.add(line);
		}
		
		return String.join ("\n",lines);
	}
	private <T> T NotImplemented()
	{
		throw new RuntimeException("Not Implemented");
	}
	private String getRequestPath(String requestHeader)
	{
		return null;
	}
	private void handleConnectionForPath(Socket connection, String path,boolean inputAllowed) throws IOException {
		if (path.equals("/stream.m3u"))
		{
			printStreamM3U(connection);
		}
		else if (path.equals("/stream"))
		{
			redirectStream(connection);
		}
		else if (path.equals("/stream_in"))
		{
			if (inputAllowed)
			{
				redirectInStream(connection);
			}
			else
			{
				rejectInStream(connection);
			}
		}
		else if (path.equals("/") || path.equals("/index.html"))
		{
			printIndexPage(connection);
		}
		else			
		{
			printNotFound(connection);
		}
		
	}

	private void rejectInStream(Socket connection) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
		writer.write(("HTTP/1.1 403 Forbidden\r\n\r\n"));
		writeDocument("rejected.html", writer);
		writer.close();
	}

	private void writeDocument(String res,BufferedWriter writer) throws IOException
	{
		try
		{
			StringWriter swriter = new StringWriter();
			synchronized(this)
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResource("rsrc/" +res).openStream()));
				int read = reader.read();
				while (read != -1)
				{
					swriter.write(read);
					read = reader.read();
				}
				reader.close();
			}
			writer.write(swriter.toString());
			writer.flush();
			
		}
		catch (IOException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new IOException(e);
		}
		
	}
	private void printIndexPage(Socket connection) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
		writer.write(("HTTP/1.1 200 OK\r\n\r\n"));
		writeDocument("index.html",writer);
		writer.close();
	}

	private void printNotFound(Socket connection) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
		writer.write(("HTTP/1.1 404 Not Found\r\n\r\n"));
		writeDocument("notfound.html", writer);
		writer.close();
	}

	private void redirectInStream(Socket connection) throws IOException {
		byte[][] chunk = new byte[][] { null};
		int[] size = new int [] {-1};
		while (true)
		{
			readChunk(connection, chunk, size);
			outs.write(chunk[0], 0, size[0]);
		}
		
	}

	private int readChunkSize(Socket connection) throws IOException
	{
		InputStream in=connection.getInputStream();
		StringWriter bout = new StringWriter();
		
		Reader reader = new InputStreamReader(in,StandardCharsets.US_ASCII);
		int ch = reader.read();
		boolean cr = false;
		boolean lf = false;
		while (ch != -1 && (!cr || !lf))
		{
			bout.write(ch);
			ch = reader.read();
			if ((ch)=='\r')
			{
				cr = true;
				lf = false;
			}
			else if (cr)
			{
				if ((ch=='\n'))
				{
					lf = true;
				}
				else
				{
					throw new IOException("Bad CRLF");
				}
			}
			else
			{
				bout.write(ch);
			}
		}
		Reader r = new StringReader(bout.toString());
		ch = r.read();
		int size = 0;
		while (ch != -1)
		{
			char hex = Character.toUpperCase((char)ch);
			size *= 16;
			if (hex >= '0' && hex <= '9')
			{
				
				size += (hex - '0');
			}
			else if (hex >= 'A' && hex <= 'F')
			{
				size += 10 + (hex - 'A');
			}
			ch = r.read();
		}
		return size;
	}
	private void readChunk(Socket connection,byte[][] chunkHandle,int[] sizeHandle) throws IOException
	{
		
		if (chunkHandle.length==1 && sizeHandle.length == 1)
		{
			sizeHandle[0] = readChunkSize(connection);
			if (chunkHandle[0] == null || chunkHandle.length < sizeHandle[0])
			{
				chunkHandle[0] = new byte[sizeHandle[0]];
			}
			connection.getInputStream().read(chunkHandle[0],0,sizeHandle[0]);
		}
	}
	private void redirectStream(Socket connection) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
		writer.write(("HTTP/1.1 200 OK\r\n\r\n"));
		writer.flush();
		outs.addOutputStream(connection.getOutputStream());
		
	}

	private void printStreamM3U(Socket connection) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
		writer.write(("HTTP/1.1 200 OK\r\n\r\n"));
		writer.write("#EXTM3U\r\n");
		writer.write("#EXTINF:-1,BitTosser Stream\r\n");
		writer.write("http://" + connection.getLocalAddress().getHostAddress() + ":" + httpPort + "/stream\r\n");
		writer.flush();
	}
}
