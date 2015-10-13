package com.saascp.installer.main;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger 
{
	private PrintWriter logw = null;
	
	public Logger(String file)
	{
		//ENSURE FILE IS SPECIFIED
		if(file.equals(""))
		{
			System.out.println("You must specify a valid file to write to!");
			System.exit(1);
		}
		
		try
		{
			this.logw = new PrintWriter( new FileWriter(file) );
		}
		catch (FileNotFoundException e)
		{
			e.getMessage();
			System.exit(1);
		}
		catch (IOException e)
		{
			e.getMessage();
			System.exit(1);
		}
	}
	
	public PrintWriter returnPrintWriter()
	{
		return this.logw;
	}
	
	public void write(String line)
	{
		this.logw.write(line + "\n");
	}
	
	public void close()
	{
		this.logw.close();
	}
	
	public void write_and_close(String line)
	{
		this.write(line);
		this.close();
	}
	
	public void write_and_print(String line)
	{
		System.out.println(line);
		this.write(line);
	}
}
