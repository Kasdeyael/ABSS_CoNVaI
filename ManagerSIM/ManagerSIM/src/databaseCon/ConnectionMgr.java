package databaseCon;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import exception.IncompatibleParameterException;

public class ConnectionMgr {

	private String sUrl;
	private String sUsr;
	private String sPass;
	Connection con = null;

	/**
	 * Constructor for Connection
	 * @param sUrl url to connect to the DB
	 * @param sUsr user to connect
	 * @param sPass password
	 */
	public ConnectionMgr(String sUrl, String sUsr, String sPass) {

		this.sUrl = sUrl;
		this.sUsr = sUsr;
		this.sPass = sPass;
	}

	/**
	 * Returns the connection to the DB.
	 * @return Connection
	 * @throws IncompatibleParameterException
	 */
	public Connection getConnection() throws IncompatibleParameterException {

		if(con != null) throw new IncompatibleParameterException("No parallel connections allowed until finished.");
		try {
			con = DriverManager.getConnection(sUrl, sUsr, sPass);
			return con;

		}catch (SQLException ex) {
			throw new IncompatibleParameterException("Connection to the DB could not be established.");
		}

	}

	/**
	 * Closes open connection to the DB.
	 * @throws IncompatibleParameterException
	 */
	public void closeConnection() throws IncompatibleParameterException {
		try {
			if(con!=null) con.close();
			con = null;
		} catch (SQLException e) {

			throw new IncompatibleParameterException("Connection to the DB could not be closed.");
		}

	}

}
