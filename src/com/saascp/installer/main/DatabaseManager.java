package com.saascp.installer.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.ld.sqlconnector.DatabaseFactory;
import com.ld.sqlconnector.DatabaseInterface;

public class DatabaseManager 
{
	private DatabaseInterface dbi = null;
	private Logger log = null;
	private String error_string;

	public DatabaseManager(Logger log)
	{
		this.log = log;
		this.dbi = DatabaseFactory.getDatabaseConnection("MySQL");
		this.error_string = "";
	}

	public String getErrorString()
	{
		return this.error_string;
	}

	public boolean setupDB(String mysql_password)
	{
		this.log.write_and_print("NOW CONNECTING TO DATABASE ...");
		try
		{
			if(mysql_password == null)
				this.dbi.setAuthentication("root", "", "");
			else
				this.dbi.setAuthentication("root", mysql_password, "");
		}
		catch(IllegalArgumentException e)
		{
			this.log.write_and_print("{CATCH} => CONNECTING TO DATABASE FAILED!\n");
			this.log.write_and_print("DBM setupDB Fatal Error " + e.getMessage() );
			return false;
		}
		this.log.write_and_print("CONNECTING TO DATABASE COMPLETE!\n");
		return true;
	}

	public boolean updateRootPassword(String pwd)
	{
		//RUN SQL MYSQL
		String sql = String.format("UPDATE mysql.user SET password=PASSWORD('%s') WHERE User='root' LIMIT 1;", pwd);
		this.dbi.setSQL(sql);
		if(!dbi.executeQuery())
		{
			this.log.write_and_print("{CATCH} => UPDATING MARIADB ROOT PASSWD FAILED!\n");
			this.log.write_and_print("Fatal Error " + dbi.getQueryErrorMessage() );
			return false;
		}

		this.dbi.setSQL("FLUSH PRIVILEGES;");
		if(!dbi.executeQuery())
		{
			this.log.write_and_print("{CATCH} => FLUSHING MARIADB PRIVILEGS FAILED!\n");
			this.log.write_and_print("Fatal Error " + dbi.getQueryErrorMessage() );
			return false;
		}
		this.log.write_and_print("SETTING MARIADB ROOT PWD TO:" + pwd + "\n");
		return true;
	}

	public boolean runUpdateQuery(String table, String key, String value)
	{
		//RUN SQL MYSQL
		this.dbi.setSQL("UPDATE " + table + " SET `key`=? WHERE `value`=? LIMIT 1;");
		this.dbi.addPreparedArgument(key);
		this.dbi.addPreparedArgument(value);
		if(!dbi.executeQuery())
		{
			this.error_string = this.dbi.getQueryErrorMessage();
			return false;
		}
		return true;
	}

	public void importDatabase()
	{
		this.log.write_and_print("NOW IMPORTING DATABASE TABLES...");
		try
		{
			InputStream is = getClass().getResourceAsStream("/InstallFiles/newdatabase.sql");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			String line_sql = "";
			while( (line=br.readLine()) != null)
			{
				//APPEND SQL TO LINE
				line_sql += line;

				//IF THE LINE CONTAINS ';' EXECUTE IT
				if(line.contains(";"))
				{
					//EXECUTE
					this.dbi.setSQL(line_sql);
					if(!dbi.executeQuery())
					{
						this.log.write_and_print("SQL CODE: [" + line_sql + "]");
						this.log.write_and_print("SQL IMPORT ERROR: " + dbi.getQueryErrorMessage());
					}

					//SET TO BLANK
					line_sql = "";
				}
			}
			br.close();
			this.log.write_and_print("IMPORTING DATABASE TABLES FINISHED");
		}
		catch (IOException | NullPointerException e)
		{
			System.out.println("SQL EXCEPTION:" + e.getMessage() );
		}
	}

	public void close()
	{
		this.dbi.closeConnection();
	}
}