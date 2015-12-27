package com.fer.hr.datasources.connection;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fer.hr.datasources.datasource.SaikuDatasource;
import com.fer.hr.olap.util.exception.SaikuOlapException;

import com.fer.hr.datasources.connection.ISaikuConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleConnectionManager extends AbstractConnectionManager {
	private Map<String, ISaikuConnection> connections = new HashMap<>();
	private final List<String> errorConnections = new ArrayList<>();
	private static final Logger log = LoggerFactory.getLogger(SimpleConnectionManager.class);

	@Override
	public void init() throws SaikuOlapException {
		try {
			this.connections = getAllConnections();
		} catch (SaikuOlapException e) {
			log.error("Error getting connections", e);
		}
	}
	
//	@Override
//	public void destroy() throws SaikuOlapException {
//		if (connections != null && !connections.isEmpty()) {
//			for (ISaikuConnection con : connections.values()) {
//				try {
//					Connection c = con.getConnection();
//						if (!c.isClosed()) {
//							c.close();
//						}
//					} catch (Exception e) {
//						log.error("Error destroying connections", e);
//					}
//				}
//			}
//        if (connections != null) {
//            connections.clear();
//        }
//	}

	@Override
	protected ISaikuConnection getInternalConnection(String name, SaikuDatasource datasource) throws SaikuOlapException {

		ISaikuConnection con;

		if (!connections.containsKey(name)) {
			con = connect(name, datasource);
			if (con != null) {
				connections.put(name, con);
			} else {
				if (!errorConnections.contains(name)) {
					errorConnections.add(name);
				}
			}

		} else {
			con = connections.get(name);
		}
		return con;
	}

	@Override
	protected ISaikuConnection refreshInternalConnection(String name, SaikuDatasource datasource) {
		try {
			ISaikuConnection con = connections.remove(name);
			if (con != null) {
				con.clearCache();
			}
			return getInternalConnection(name, datasource);
		} catch (Exception e) {
			log.error("Could not get internal connection", e);
		}
		return null;
	}

	private ISaikuConnection connect(String name, SaikuDatasource datasource) throws SaikuOlapException {
		if (datasource != null) {

			try {
				ISaikuConnection con = SaikuConnectionFactory.getConnection(datasource);
				if (con.initialized()) {
					return con;
				}
			} catch (Exception e) {
				log.error("Could not get connection", e);
			}

			return null;
		}

		throw new SaikuOlapException("Cannot find connection: (" + name + ")");
	}
}
