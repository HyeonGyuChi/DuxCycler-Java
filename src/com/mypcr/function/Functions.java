package com.mypcr.function;

import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;

import com.mypcr.beans.Action;

public class Functions 
{
	private static boolean isMac = false;
	private static String pcrPath = null;
	private static String logpath = null;
	private static String logFilePath = null;
	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	private static boolean isLogging = false;
	private static String serialNumber = null;

	static{
		String os = System.getProperty("os.name", "win").toLowerCase();
		
		if( os.indexOf("win") != -1 ){
			isMac = false;
		}else if( os.indexOf("mac") != -1 ){
			isMac = true;
		}
		
		if( !isMac)
			pcrPath = "C:\\mPCR";
		else{
			String classPath = System.getProperty("java.class.path");
			String[] tempPath = classPath.split("/");
			for(int i=0; i<tempPath.length-1; ++i){
				pcrPath += tempPath[i] + "/";
			}
			
			pcrPath += "mPCR";
		}
		
		logpath = pcrPath + (isMac ? "/log" : "\\log");
		
		String dateFormat = df.format(new Date());
		logFilePath = logpath + (isMac ? ("/log_" + dateFormat + ".txt") : ("\\log_" + dateFormat + ".txt"));
	}
	
	public static void setLogSerialNumber(String serialNumber){
		Functions.serialNumber = serialNumber;
	}
	
	public static void log(String message)
	{
		if( isLogging ){
			File logFile = new File(logpath);	logFile.mkdirs();
			logFile = new File(logFilePath);
			
			String dateFormat = "[" + df.format(new Date()) + ", + " + serialNumber + "] "; 
			
			try {
				PrintWriter out = new PrintWriter(new FileWriter(logFile, true));
				out.println(dateFormat + message);
				out.close();
			} catch (IOException e) {
				// 	TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void setLogging(boolean isLogging){
		Functions.isLogging = isLogging; 
	}
	
	public static String Get_RecentProtocolPath()
	{
		String save_path = pcrPath;
		File file = new File(save_path);	file.mkdir();
		
		if( !isMac )
			save_path += "\\recent_protocol.txt";
		else
			save_path += "/recent_protocol.txt";
		File RecentFile = new File(save_path);
		BufferedReader in = null;
		
		try
		{	
			in = new BufferedReader(new FileReader(RecentFile));
			String input = in.readLine();
			in.close();
			
			return input;
		} catch (IOException e)
		{
			return null;
		}
	}
	
	public static void Save_RecentProtocol(String protocol_path)
	{
		String save_path = pcrPath;
		File file = new File(save_path);	file.mkdir();
		if( !isMac )
			save_path += "\\recent_protocol.txt";
		else
			save_path += "/recent_protocol.txt";
		File RecentFile = new File(save_path);
		
		BufferedReader in = null;
		PrintWriter out = null;
		try
		{	
			in = new BufferedReader(new FileReader(RecentFile));
			in.close();
			out = new PrintWriter(new FileWriter(RecentFile));
			out.println(protocol_path);
			out.close();
		} catch (IOException e)
		{
			try
			{
				in = null;
				RecentFile.createNewFile();
				Save_RecentProtocol(protocol_path);
			}catch(IOException e2){}
		}
	}
	
	public static Action[] ReadProtocolbyPath( String path ) throws Exception
	{
		BufferedReader in = null;
		ArrayList<String> inData = new ArrayList<String>();
		String tempData = null;
		
		// ���Ϸκ��� �����͸� �о
		in = new BufferedReader(new InputStreamReader(new FileInputStream( path )));
		
		while( (tempData = in.readLine()) != null )
		{
			inData.add( tempData );
		}
			
		in.close();
		
		// PCR Protocol �������� Ȯ��
		// �� ���ٰ� �� ���ٿ� %PCR%, %END% �� �ִ��� Ȯ��
		String start = inData.get(0);
		String end   = inData.get(inData.size()-1);
		int actionCount = inData.size()-2;
		Action[] actions = new Action[actionCount];
		
		if( !start.contains("%PCR%") )
			return null;
		if( !end.contains("%END") )
			return null;
		
		String token = "\\\\";
		
		if( isMac )
			token = "/";
		
		String[] tokens = path.split(token);
		
		for(int i=1; i<inData.size()-1; i++)
		{
			String[] datas = inData.get(i).split("\t{1,}| {1,}");
			actions[i-1] = new Action(tokens[tokens.length-1]);
			int j=0;
			for( String temp : datas )
			{	
				actions[i-1].set(j++, temp);
			}
		}
		
		return actions;
	}

	public static Action[] ReadProtocolbyDialog( JFrame parent )
	{
		// ���� ���� ���̾�α� ����
		FileDialog fileDialog = new FileDialog(parent, "Select protocol file", FileDialog.LOAD);
		fileDialog.setFile("*.txt");
		fileDialog.setVisible(true);
		
		String dir = fileDialog.getDirectory();
		String file = fileDialog.getFile();
		String path = dir + file;
		
		if( dir == null )
		{
			Action[] action = new Action[1];
			action[0] = new Action();
			return action;
		}
		
		BufferedReader in = null;
		ArrayList<String> inData = new ArrayList<String>();
		String tempData = null;
		
		// ���Ϸκ��� �����͸� �о
		try
		{
			in = new BufferedReader(new InputStreamReader(new FileInputStream( path )));
			
			while( (tempData = in.readLine()) != null )
			{
				inData.add( tempData );
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}finally
		{
			try
			{
				in.close();
			}catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		
		// PCR Protocol �������� Ȯ��
		// �� ���ٰ� �� ���ٿ� %PCR%, %END% �� �ִ��� Ȯ��
		String start = inData.get(0);
		String end   = inData.get(inData.size()-1);
		int actionCount = inData.size()-2;
		Action[] actions = new Action[actionCount];
		
		if( !start.contains("%PCR%") )
			return null;
		if( !end.contains("%END") )
			return null;
		
		for(int i=1; i<inData.size()-1; i++)
		{
			String[] datas = inData.get(i).split("\t{1,}| {1,}");
			actions[i-1] = new Action(file);
			int j=0;
			for( String temp : datas )
			{
				actions[i-1].set(j++, temp);
			}
		}
		
		if( actions[0].getLabel() != null )
			Save_RecentProtocol(path);
		
		return actions;
	}
}
