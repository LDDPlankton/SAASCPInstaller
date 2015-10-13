package com.saascp.installer.main;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ld.libcmdexecutor.CommandExecutor;
import com.ld.libcmdexecutor.CommandManager;
import com.systemlibrary.service.email.EmailUtil;

public class Installer
{
	private CommandManager cmd_mgr = null;
	private CommandExecutor cmd = null;
	private Logger log = null;
	private DatabaseManager dbm = null;
	private MySQLConfigManager mcm = null;
	private String version = "1.00";

	public Installer()
	{
		this.log = new Logger("/var/log/saascp-install.log");
		this.cmd = new CommandExecutor();
		this.cmd_mgr = new CommandManager();
		this.dbm = new DatabaseManager(this.log);
		this.mcm = new MySQLConfigManager();
		this.cmd.setShowCommandOutputWhileRunning(false);			//SHOW CMDS AS NOT RUNNING
	}

	public boolean run()
	{
		boolean install_status = true;
		boolean status;

		//GET TIME + FORMAT FOR USE
		Date tDate = new Date();
		SimpleDateFormat sDate = new SimpleDateFormat("MM/dd/YY HH:mm:ss");

		String welcome = "============= SAASCP INSTALLER VERSION " + this.version + " =============\n" +
				"Installer Start Time: " + sDate.format(tDate);

		//PRINT +WRITE WELCOME MESSAGE
		this.log.write_and_print(welcome);

		//GENERATE MYSQL ROOT PASS
		String mysql_password = mcm.generatePassword(15);
		mcm.removeConfigFile();

		//RUN SYSTEM COMMANDS
		system_commands(mysql_password);

		//WRITE MYSQL CONFIG FILE
		mcm.writeConfigFile("root", mysql_password );

		//CREATE SYSTEM DIRECTORIES
		this.create_system_directories();

		//HANDLE DB
		this.create_database();
		status = this.dbm.setupDB(null);

		//IF SETUP OF DB FAILED
		if(!status)
		{
			install_status = false;
		}
		System.out.println("STATUS="+status);

		//SET MYSQL PWD
		if(status)
		{
			this.dbm.updateRootPassword(mysql_password);
		}

		//IF SETUP OF DB DID NOT FAIL
		if(status)
		{
			//this.dbm.importDatabase();

			//RUN QUERIES IN FUTURE HERE...
			this.dbm.close();
		}

		//SETUP POSTFIX
		status = this.processPostFixEmailSetup();
		if(!status)
		{
			install_status = false;
			System.out.println("Unable to add PostFix Configuration Files!");
		}		

		if(!install_status)
		{
			System.out.println("The install of SAASCP FAILED! PLEASE CONTACT SUPPORT!");
		}
		else
		{
			System.out.println("The install of SAASCP has now COMPLETED!");
		}

		//CLOSE LOGGER
		this.log.close();
		return install_status;
	}

	private boolean create_system_directories()
	{
		return true;
		/*
		List<String> dir_list = new ArrayList<String>();
		boolean mkdir_status;
		boolean status = true;
		dir_list.add("/usr/local/saascp");
		dir_list.add("/usr/local/saascp/logs");
		dir_list.add("/usr/local/saascp/scripts");
		dir_list.add("/usr/local/saascp/public_html");
		
		//LOOP + MKDIRS
		for(String item: dir_list)
		{
			File new_dir = new File(item);
			mkdir_status = new_dir.mkdir();

			if(mkdir_status)
			{
				status = false;
				
				//WRITE + PRINT COMMAND BEING RUN
				this.log.write_and_print("Making directory [OK]: " + new_dir.getAbsolutePath());				
			}
			else
			{
				//WRITE + PRINT COMMAND BEING RUN
				this.log.write_and_print("Making directory [FAILED]: " + new_dir.getAbsolutePath());
			}
		}
		return status;
		*/
	}

	public void system_commands(String mysql_password)
	{
		//ADD COMMANDS TO QUEUE
		this.cmd_mgr.addCommandToQueue("yum remove httpd-* php-* mariadb mariadb-devel mariadb-server -y", 0, false);
		this.cmd_mgr.addCommandToQueue("yum install bind* mariadb mariadb-devel mariadb-server postfix dovecot httpd httpd-devel openssl-devel mysql-* gcc make autoconf -y", 0, false);
		this.cmd_mgr.addCommandToQueue("systemctl enable named", 0, false);
		this.cmd_mgr.addCommandToQueue("systemctl enable postfix", 0, false);
		this.cmd_mgr.addCommandToQueue("systemctl enable dovecot", 0, false);
		this.cmd_mgr.addCommandToQueue("systemctl enable mariadb", 0, false);
		this.cmd_mgr.addCommandToQueue("systemctl enable httpd", 0, false);
		this.cmd_mgr.addCommandToQueue("systemctl start named", 0, false);
		this.cmd_mgr.addCommandToQueue("systemctl start postfix", 0, false);
		this.cmd_mgr.addCommandToQueue("systemctl start dovecot", 0, false);
		this.cmd_mgr.addCommandToQueue("systemctl start mariadb", 0, false);
		this.cmd_mgr.addCommandToQueue("systemctl start httpd", 0, false);
		this.cmd_mgr.addCommandToQueue("ln -s /var/lib/mysql/mysql.sock /tmp/mysql.sock", 0, true);

		//RUN CMDS
		this.cmd_mgr.runCommandQueue();
		
		//GET DATA TO WRITE
		this.log.write( this.cmd_mgr.getDataToWrite() );
	}

	public void create_database()
	{
		/*
		this.cmd_mgr.addCommandToQueue("/usr/bin/mysqladmin create dbname", 0, false);
		this.cmd_mgr.runCommandQueue();

		//GET DATA TO WRITE
		this.log.write( this.cmd_mgr.getDataToWrite() );
		*/
	}

	public boolean processPostFixEmailSetup()
	{
		try
		{
			File fd1 = new File(EmailUtil.vmail_aliases_file);
			File fd2 = new File(EmailUtil.vmail_domains_file);
			File fd3 = new File(EmailUtil.vmail_mailbox_file);
			File fd4 = new File(EmailUtil.vmail_virtual_uid);

			fd1.createNewFile();
			fd2.createNewFile();
			fd3.createNewFile();
			fd4.createNewFile();

			return true;
		}
		catch (IOException e)
		{
			System.out.println("Unable to create all required Email Aliase Files: " + e.getMessage() );
			return false;
		}
	}

}

