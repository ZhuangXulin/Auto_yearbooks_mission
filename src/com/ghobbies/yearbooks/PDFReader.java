/**
 * 
 */
package com.ghobbies.yearbooks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

/**
 * @author Xulin Zhuang
 * 
 */
public class PDFReader {

	static Connection conn;
	static Statement stmt;

	public void pdfTransformationTxt(String file) throws Exception {
		// 是否排序
		boolean sort = false;
		// pdf文件名
		String pdfFile = file;
		// 输入文本文件名称
		String textFile = null;
		// 编码方式
		String encoding = "UTF-8";
		// 开始提取页数
		int startPage = 1;
		// 结束提取页数
		int endPage = Integer.MAX_VALUE;
		// 文件输入流，生成文本文件
		Writer output = null;
		// 内存中存储的PDF Document
		PDDocument document = null;
		try {
			try {
				// 首先当作一个URL来装载文件，如果得到异常再从本地文件系统//去装载文件
				URL url = new URL(pdfFile);
				// 注意参数已不是以前版本中的URL.而是File。
				document = PDDocument.load(pdfFile);
				// 获取PDF的文件名
				String fileName = url.getFile();
				// 以原来PDF的名称来命名新产生的txt文件
				if (fileName.length() > 4) {
					File outputFile = new File(fileName.substring(0,
							fileName.length() - 4)
							+ ".txt");
					textFile = outputFile.getName();
				}
			} catch (MalformedURLException e) {
				// 如果作为URL装载得到异常则从文件系统装载
				// 注意参数已不是以前版本中的URL.而是File。
				document = PDDocument.load(pdfFile);
				if (pdfFile.length() > 4) {
					textFile = pdfFile.substring(0, pdfFile.length() - 4)
							+ ".txt";
				}
			}
			// 文件输入流，写入文件倒textFile
			output = new OutputStreamWriter(new FileOutputStream(textFile),
					encoding);
			// PDFTextStripper来提取文本
			PDFTextStripper stripper = null;
			stripper = new PDFTextStripper();
			// 设置是否排序
			stripper.setSortByPosition(sort);
			// 设置起始页
			stripper.setStartPage(startPage);
			// 设置结束页
			stripper.setEndPage(endPage);
			// 调用PDFTextStripper的writeText提取并输出文本
			stripper.writeText(document, output);

		} finally {
			if (output != null) {
				// 关闭输出流
				output.close();
			}
			if (document != null) {
				// 关闭PDF Document
				document.close();
			}
		}
	}

	public void readTxtIntoDB(String txtFile) {
		int start = 0;
		int end = 0;
		// 模糊年份时间段
		String fuzzyDateQuantum = "";
		// 年鉴描述
		String description = "";
		// 准备输入状态，当读取到年份的时候，就激活此状态
		boolean inputStatus = false;
		// 入库状态，当入库状态为true时，表示数据缓存还没有入库
		boolean insertDBStatus = false;
		try {
			String encoding = "UTF-8";
			File file = new File(txtFile);
			// 判断文件是否存在
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file), encoding);
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;

				// 连接数据库
				PDFReader.getConnect();
				// 读取内容
				while ((lineTxt = bufferedReader.readLine()) != null) {
					start = lineTxt.indexOf("【");
					end = lineTxt.indexOf("】");
					// System.out.println(start);
					// System.out.println(end);
					if (start >= 0 && end > 0) {
						if (!insertDBStatus) {
							inputStatus = true;
							fuzzyDateQuantum = lineTxt
									.substring(start + 1, end);
							System.out.println(lineTxt
									.substring(start + 1, end));
							insertDBStatus = true;
						} else {
							// 进行入库操作
							PDFReader.addYearbookInfo(
									PDFReader.getPersonID(),
									fuzzyDateQuantum, description);
							fuzzyDateQuantum = "";
							description = "";
							// 入库后再次读取内容
							inputStatus = true;
							fuzzyDateQuantum = lineTxt
									.substring(start + 1, end);
							System.out.println(lineTxt
									.substring(start + 1, end));
						}

					}else if (inputStatus) {
						description += lineTxt;
					}

					// System.out.println(lineTxt);
				}
				read.close();
			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		// 关闭数据库
		PDFReader.closeConnection(stmt, conn);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// pdf转换
		PDFReader pdfReader = new PDFReader();
		/*
		 * try { // pdfReader
		 * .pdfTransformationTxt("/Users/ZhuangXulin/Downloads/zhouenlai.pdf");
		 * } catch (Exception e) { e.printStackTrace(); }
		 */
		// txt录入
		pdfReader.readTxtIntoDB("/Users/ZhuangXulin/Downloads/zhouenlai.txt");
	}

	/**
	 * 获取录入的年鉴用户ID
	 * 
	 * @return
	 */
	private static int getPersonID() {
		return 51;
	}

	/**
	 * 
	 */
	private static void getConnect() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("加载驱动失败");
			e.printStackTrace();
		}
		try {
			conn = DriverManager.getConnection("jdbc:mysql://db.ghobbies.com:3306/yearbooks_development", "yearbooks", "ZhuangXulin2003YearBooks");
			stmt = conn.createStatement();
			System.out.println("数据库连接成功");
		} catch (SQLException e) {
			System.out.println("数据库连接失败");
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param stmt
	 * @param conn
	 */
	private static void closeConnection(Statement stmt, Connection conn) {
		try {
			if (stmt != null) {
				stmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			System.out.println("关闭数据库失败");
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param personString
	 * @param year
	 * @param description
	 * @return
	 */
	private static boolean addYearbookInfo(int personID, String year,
			String eventDescription) {
		String sql = "insert into people_yearbooks"
				+ "(person_id,isfuzzydate,isfuzzydatequantum,fuzzy_date_quantum,event_description,created_at,updated_at) "
				+ "values("+personID+",0,1,'"+year+"','"+eventDescription+"',now(),now())";
		System.out.println("sql:"+sql);
		try {
			stmt.clearBatch();
			stmt.addBatch(sql);
			stmt.executeBatch();
			System.out.println("插入数据成功");
		} catch (SQLException e) {
			System.out.println("插入数据失败");
			e.printStackTrace();
		}
		return true;
	}
}
