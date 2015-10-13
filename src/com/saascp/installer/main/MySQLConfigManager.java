package com.saascp.installer.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MySQLConfigManager
{
	private Map<String, String> file_config;
	
	public MySQLConfigManager()
	{
		this.file_config = new HashMap<String, String>();
	}
	
	public String generatePassword(int length)
	{
		String passwd_chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!@#$^&*()";
		String passwd_str = "";
		Random rnd = new Random();
		int count = 0;
		
		//ENSURE WE MEET PASSWORD REQUIREMENT
		while(count < length)
		{
			int index = rnd.nextInt(passwd_chars.length()-1);
			passwd_str += passwd_chars.charAt(index);
			count++;
		}
		
		return passwd_str;
	}
	
	//GET VALUES FROM my.cnf
	public String getValue(String key)
	{
		return this.file_config.get(key);
	}
	
	public void writeConfigFile(String username, String password)
	{
		this.removeConfigFile();
		try
		{
			BufferedWriter br = new BufferedWriter( new FileWriter("/root/.my.cnf") );
			br.write("[client]" + "\n");
			br.write("user="+username + "\n");
			br.write("pass=\""+password + "\"\n");
			br.close();
		}
		catch (IOException e)
		{
			System.out.println("ERROR MCM writeConfigFile(): " + e.getMessage() );
		}
		
	}
	
	public void removeConfigFile()
	{
		File myFile = new File("/root/.my.cnf");
		
		//IF EXISTS ... REMOVE
		if(myFile.exists())
			myFile.delete();
	}
	
	public void readConfigFile()
	{
		try
		{
			BufferedReader in = new BufferedReader( new FileReader("/root/.my.cnf") );
			String line;
			while( (line=in.readLine()) != null)
			{
				String key = line.split("=")[0];
				String val = line.split("=")[1].replaceAll("\"", "");
				
				//System.out.println("K=" + key + " V="+val);
				this.file_config.put(key, val);
			}
			in.close();
		}
		catch (IOException e)
		{
			System.out.println("ConfigManager::readConfigFile() => " + e.getMessage() );
			System.exit(1);
		}
	}
}
