package com.webuildsystems.chazzofalf.bytePasserServer.ByteTosserServer.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

import com.webuildsystems.chazzofalf.bytePasserServer.ByteTosserServer.ByteTosserServer;

public class ByteTosserHttpServer extends ByteTosserServer 
{
	
	@Override
	protected void handleOutputSocket(Socket outputSocket) throws IOException {
		// TODO Auto-generated method stub
		String requestHeader = getRequestHeader(outputSocket);
		String path = getRequestPath(requestHeader);
		handleConnectionForPath(outputSocket,path);
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
		return NotImplemented();
	}
	private void handleConnectionForPath(Socket connection, String path) {
		// TODO Auto-generated method stub
		
	}
}
