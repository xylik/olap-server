package com.fer.hr;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;

import org.olap4j.layout.CellSetFormatter;
import org.olap4j.layout.RectangularCellSetFormatter;

import com.fer.hr.service.olap.ThinQueryService;
import com.fer.hr.olap.query2.ThinQuery;
import com.fer.hr.olap.util.ObjectUtil;
import com.fer.hr.datasources.connection.SimpleConnectionManager;
import com.fer.hr.datasources.datasource.SaikuDatasource;
import com.fer.hr.service.datasource.ClassPathResourceDatasourceManager;
import com.fer.hr.datasources.connection.IConnectionManager;
//import com.fer.hr.olap.discover.OlapMetaExplorer;
import com.fer.hr.olap.dto.SaikuCube;
import com.fer.hr.olap.dto.resultset.CellDataSet;
import com.fer.hr.service.datasource.DatasourceService;
import com.fer.hr.service.datasource.IDatasourceManager;
import com.fer.hr.service.olap.OlapDiscoverService;

public class MainDemo {
	public static boolean DEBUG = false;
	private IDatasourceManager datasourceManager;
	private IConnectionManager connectionManager;
	// private OlapMetaExplorer olapMetaExplorer;
	public OlapDiscoverService olapDiscoverService;
	public DatasourceService datasourceService;
	public ThinQueryService thinQueryService;

	public static final String mdxYears$Products_Places = "WITH\n" + "SET [~COLUMNS_Time_TimeHierarchy] AS\n"
			+ "{[Time.TimeHierarchy].[Year level].Members}\n" + "SET [~COLUMNS_Product_ProductHierarchy] AS\n"
			+ "{[Product.ProductHierarchy].[Category level].Members}\n" + "SET [~ROWS] AS\n"
			+ "{[Location.CountryHierarchy].[Place name].Members}\n" + "SELECT\n"
			+ "NON EMPTY NonEmptyCrossJoin([~COLUMNS_Time_TimeHierarchy], [~COLUMNS_Product_ProductHierarchy]) ON COLUMNS,\n"
			+ "NON EMPTY [~ROWS] ON ROWS\n" + "FROM [Sales cube]\n";

	public static void main(String[] args) {
		try {
			new MainDemo().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void run() throws Exception {
		File f = new File(System.getProperty("java.io.tmpdir") + "/files/");
		f.mkdir();

		this.datasourceManager = new ClassPathResourceDatasourceManager(System.getProperty("java.io.tmpdir") + "/files/");
		InputStream is = MainDemo.class.getResourceAsStream("/connection.properties");
		Properties testProps = new Properties();
		testProps.load(is);
		this.datasourceManager.setDatasource(new SaikuDatasource("test", SaikuDatasource.Type.OLAP, testProps));

		this.connectionManager = new SimpleConnectionManager();
		this.connectionManager.setDataSourceManager(datasourceManager);
		this.connectionManager.init();

		// this.olapMetaExplorer = new OlapMetaExplorer(connectionManager);

		this.datasourceService = new DatasourceService();
		this.datasourceService.setConnectionManager(connectionManager);

		this.olapDiscoverService = new OlapDiscoverService();
		this.olapDiscoverService.setDatasourceService(datasourceService);

		this.thinQueryService = new ThinQueryService();
		this.thinQueryService.setOlapDiscoverService(olapDiscoverService);

		SaikuCube c = olapDiscoverService.getAllCubes().get(0);
		String name = "mdx1";
		String mdx = mdxYears$Products_Places;
		ThinQuery tq = new ThinQuery(name, c, mdx);
		CellDataSet cs = thinQueryService.execute(tq);

		// TODO ouput formatted result to console
		// CellSetFormatter formatter = new RectangularCellSetFormatter(false);
		// formatter.format(cs, new PrintWriter(System.out, true));

	}

}
