package com.indexer;

import java.sql.Timestamp;
import java.util.Date;

public class Log {
	
	public static void info(String msg) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());	
		System.out.println("["+timestamp+"] " + msg);
	}

	public static void error(String msg) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.err.println("["+timestamp+"] " + msg);
	}

}