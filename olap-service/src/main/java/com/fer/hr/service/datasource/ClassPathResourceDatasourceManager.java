/*  
 *   Copyright 2014 OSBI Ltd
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.fer.hr.service.datasource;

import com.fer.hr.database.dto.MondrianSchema;
//import com.fer.hr.datasources.connection.RepositoryFile;
import com.fer.hr.datasources.datasource.SaikuDatasource;
import com.fer.hr.datasources.datasource.SaikuDatasource.Type;
//import com.fer.hr.repository.AclEntry;
//import com.fer.hr.repository.IRepositoryObject;
//import com.fer.hr.service.user.UserService;
import com.fer.hr.service.util.exception.SaikuServiceException;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

//import javax.jcr.RepositoryException;

public class ClassPathResourceDatasourceManager implements IDatasourceManager {

	private URL repoURL;

	private final Map<String, SaikuDatasource> datasources = Collections.synchronizedMap(new HashMap<String, SaikuDatasource>());

	public ClassPathResourceDatasourceManager() {

	}

	public ClassPathResourceDatasourceManager(String path) {
		try {
			setPath(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void init() {
		if(repoURL == null) {
			File f = new File(System.getProperty("java.io.tmpdir") + "/files/");
			f.mkdir();
			setPath(System.getProperty("java.io.tmpdir") + "/files/");
			
			InputStream inputStream = ClassPathResourceDatasourceManager.class.getResourceAsStream("/connection.properties");
			Properties testProps = new Properties();
			try {
				testProps.load(inputStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
			String connStr = System.getenv("DATABASE_URL");
			if(connStr != null && !connStr.isEmpty()) {
			    URI dbUri = null;
				try {
					dbUri = new URI(connStr);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			    String username = dbUri.getUserInfo().split(":")[0];
			    String password = dbUri.getUserInfo().split(":")[1];
			    String mondrianJdbcKey = "jdbc:mondrian:Jdbc=";
			    String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
				String catalog = ";Catalog=res:schemas/MondrianSalesSchema.xml;";
				String dbConnStr = mondrianJdbcKey + dbUrl + catalog;
				testProps.replace("location", dbConnStr);
				testProps.replace("username", username);
				testProps.replace("password", password);
				System.out.println("ClassPathResourceDatasourceManager -> init() -> props=" + testProps.toString());
			}
			setDatasource( new SaikuDatasource("test", SaikuDatasource.Type.OLAP, testProps) );
		}
	}

	private void setPath(String path) {

		FileSystemManager fileSystemManager;
		try {
			fileSystemManager = VFS.getManager();

			FileObject fileObject;
			fileObject = fileSystemManager.resolveFile(path);
			if (fileObject == null) {
				throw new IOException("File cannot be resolved: " + path);
			}
			if (!fileObject.exists()) {
				throw new IOException("File does not exist: " + path);
			}
			repoURL = fileObject.getURL();
			if (repoURL == null) {
				throw new Exception("Cannot load connection repository from path: " + path);
			} else {
				load();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void load() {
		datasources.clear();
		try {
			if (repoURL != null) {
				File[] files = new File(repoURL.getFile()).listFiles();

				for (File file : files) {
					if (!file.isHidden()) {
						Properties props = new Properties();
						props.load(new FileInputStream(file));
						String name = props.getProperty("name");
						String type = props.getProperty("type");
						if (name != null && type != null) {
							Type t = SaikuDatasource.Type.valueOf(type.toUpperCase());
							SaikuDatasource ds = new SaikuDatasource(name, t, props);
							datasources.put(name, ds);
						}
					}
				}
			} else {
				throw new Exception("repo URL is null");
			}
		} catch (Exception e) {
			throw new SaikuServiceException(e.getMessage(), e);
		}
	}

	public void unload() {

	}

	public SaikuDatasource addDatasource(SaikuDatasource datasource) {
		try {
			String uri = repoURL.toURI().toString();
			if (uri != null && datasource != null) {
				uri += datasource.getName().replace(" ", "_");
				File dsFile = new File(new URI(uri));
				if (dsFile.exists()) {
					dsFile.delete();
				} else {
					dsFile.createNewFile();
				}
				FileWriter fw = new FileWriter(dsFile);
				Properties props = datasource.getProperties();
				props.store(fw, null);
				fw.close();
				datasources.put(datasource.getName(), datasource);
				return datasource;

			} else {
				throw new SaikuServiceException("Cannot save datasource because uri or datasource is null uri(" + (uri == null) + ")");
			}
		} catch (Exception e) {
			throw new SaikuServiceException("Error saving datasource", e);
		}
	}

	public SaikuDatasource setDatasource(SaikuDatasource datasource) {
		return addDatasource(datasource);
	}

	public List<SaikuDatasource> addDatasources(List<SaikuDatasource> datasources) {
		for (SaikuDatasource ds : datasources) {
			addDatasource(ds);
		}
		return datasources;
	}

	public boolean removeDatasource(String datasourceName) {
		try {
			String uri = repoURL.toURI().toString();
			if (uri != null) {
				// seems like we don't have to do this anymore
				// uri.toString().endsWith(String.valueOf(File.separatorChar))) {
				uri += datasourceName;
				File dsFile = new File(new URI(uri));
				if (dsFile.delete()) {
					datasources.remove(datasourceName);
					return true;
				}
			}
			throw new Exception("Cannot delete datasource file uri:" + uri);
		} catch (Exception e) {
			throw new SaikuServiceException("Cannot delete datasource", e);
		}
	}

	public boolean removeSchema(String schemaName) {
		return false;
	}

	public Map<String, SaikuDatasource> getDatasources() {
		return datasources;
	}

	public SaikuDatasource getDatasource(String datasourceName) {
		return datasources.get(datasourceName);
	}

	@Override
	public SaikuDatasource getDatasource(String datasourceName, boolean refresh) {
		return datasources.get(datasourceName);
	}

	public void addSchema(String file, String path, String name) {

	}

	public List<MondrianSchema> getMondrianSchema() {
		return null;
	}

	public MondrianSchema getMondrianSchema(String catalog) {
		return null;
	}

	// public RepositoryFile getFile(String file) {
	// return null;
	// }
	//
	// public String getFileData(String file, String username, List<String> roles) {
	// return null;
	// }
	//
	// public String getInternalFileData(String file) {
	// return null;
	// }
	//
	// public InputStream getBinaryInternalFileData(String file) throws RepositoryException {
	// return null;
	// }
	//
	// public String saveFile(String path, Object content, String user, List<String> roles) {
	// return null;
	// }
	//
	// public RepositoryFile getFile2(String file) {
	// return null;
	// }
	//
	// public String getFileData(String file) {
	// return null;
	// }
	//
	// public String saveFile(String path, String content, String user, List<String> roles) {
	// return null;
	// }
	//
	// public String removeFile(String path, String user, List<String> roles) {
	// return null;
	// }
	//
	// public String moveFile(String source, String target, String user, List<String> roles) {
	// return null;
	// }
	//
	// public String saveInternalFile(String path, Object content, String type) {
	// return null;
	// }
	//
	// public String saveBinaryInternalFile(String path, InputStream content, String type) {
	// return null;
	// }
	//
	// public String saveInternalFile(String path, String content) {
	// return null;
	// }
	//
	// public void removeInternalFile(String filePath) {
	//
	// }
	//
	// public List<IRepositoryObject> getFiles(List<String> type, String username, List<String> roles) {
	// return null;
	// }
	//
	// public List<IRepositoryObject> getFiles(List<String> type, String username, List<String> roles, String path) {
	// return null;
	// }
	//
	// public List<IRepositoryObject> getFiles(String type, String username, List<String> roles) {
	// return null;
	// }
	//
	// public List<IRepositoryObject> getFiles(String type, String username, List<String> roles, String path) {
	// return null;
	// }
	//
	// public javax.jcr.Node getFiles() {
	// return null;
	// }
	//
	// public void createUser(String user) {
	//
	// }
	//
	// public void deleteFolder(String folder) {
	//
	// }
	//
	// public AclEntry getACL(String object, String username, List<String> roles) {
	// return null;
	// }
	//
	// public void setACL(String object, String acl, String username, List<String> roles) {
	//
	// }
	//
	// public void setUserService(UserService userService) {
	//
	// }
	//
	// public List<MondrianSchema> getInternalFilesOfFileType(String type) {
	// return null;
	// }
	//
	// public void createFileMixin(String type) throws RepositoryException {
	//
	// }
	//
	// public byte[] exportRepository() {
	// return new byte[0];
	// }
	//
	// public void restoreRepository(byte[] data) {
	//
	// }
	//
	// public boolean hasHomeDirectory(String name) {
	//
	// return false;
	// }
	//
	// public void restoreLegacyFiles(byte[] data) {
	//
	// }
	//
	// public String getFoodmartschema() {
	// return null;
	// }
	//
	// public void setFoodmartschema(String schema) {
	//
	// }
	//
	// public void setFoodmartdir(String dir) {
	//
	// }
	//
	// public String getFoodmartdir() {
	// return null;
	// }
	//
	// public String getDatadir() {
	// return null;
	// }
	//
	// public void setDatadir(String dir) {
	// }
	//
	// public void setFoodmarturl(String foodmarturl) {
	//
	// }
	//
	// public String getFoodmarturl() {
	// return null;
	// }
	//
	// public String getEarthquakeUrl() {
	// return null;
	// }
	//
	// public String getEarthquakeDir() {
	// return null;
	// }
	//
	// public String getEarthquakeSchema() {
	// return null;
	// }
	//
	// public void setEarthquakeUrl(String earthquakeUrl) {
	//
	// }
	//
	// public void setEarthquakeDir(String earthquakeDir) {
	//
	// }
	//
	// public void setEarthquakeSchema(String earthquakeSchema) {
	//
	// }
	//
	// public String saveFile(String path, String content, String user) {
	// return null;
	// }
}