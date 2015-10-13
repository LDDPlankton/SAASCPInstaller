package com.saascp.installer.main;

public class Main
{
	private static final String VERSION = "1.0.0";
	
	public static void printMenu()
	{
		System.out.println("SAASCP Installer " + VERSION + "\n"
				+ "Usage: java -jar <program> <option> <args...>\n"
				+ "\tinstall						: This option will begin the install of our SAAS CP Application.\n"
				);
	}
	
	public static void main(String[] args)
	{
		String username = System.getProperty("user.name");
		if(!username.equals("root"))
		{
			System.out.println("You must run this program as root to complete the setup!");
			System.exit(-1);
		}

		//ENSURE ARGUMENTS PASSED
		if(args.length < 1)
		{
			printMenu();
			System.exit(-1);
		}

		//HANDLE BASED ON COMMAND
		switch(args[0])
		{
			case "install":
				Installer ins = new Installer();
				ins.run();
				
				break;
				
			default:
				printMenu();
		}
		
	}
}
