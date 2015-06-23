/**
 * 
 */
package com.ghobbies.yearbooks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

/**
 * @author ZhuangXulin
 * 
 */
public class YearbooksManager {

	static Connection conn;
	static Statement stmt;

	/**
	 * 获取录入的年鉴用户ID
	 * 
	 * @return
	 */
	private static int getPersonID() {
		return 52;
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
				DBManager.getConnect();
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
							YearbooksManager.addYearbookInfo(
									YearbooksManager.getPersonID(),
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

					} else if (inputStatus) {
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
		DBManager.closeConnection(stmt, conn);
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
				+ "values(" + personID + ",0,1,'" + year + "','"
				+ eventDescription + "',now(),now())";
		System.out.println("sql:" + sql);
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

	public void readExcelIntoDB(String excelFile) {
		// 连接数据库
		DBManager.getConnect();
		jxl.Workbook readwb = null;
		try {
			// 构建Workbook对象, 只读Workbook对象
			// 直接从本地文件创建Workbook
			InputStream instream = new FileInputStream(excelFile);
			readwb = Workbook.getWorkbook(instream);
			// Sheet的下标是从0开始
			// 获取第一张Sheet表
			Sheet readsheet = readwb.getSheet(0);
			// 获取Sheet表中所包含的总列数
			int rsColumns = readsheet.getColumns();
			// 获取Sheet表中所包含的总行数
			int rsRows = readsheet.getRows();
			// 获取指定单元格的对象引用
			String year = null;
			String eventDescription = null;

			for (int i = 1; i < rsRows; i++) {
				for (int j = 0; j < rsColumns; j++) {
					Cell cell = readsheet.getCell(j, i);
					if (j == 0) {
						year = cell.getContents();
					} else {
						eventDescription = cell.getContents();
					}
				}
				if (!year.isEmpty() && !eventDescription.isEmpty()) {
					System.out.println(year + ":" + eventDescription);
					YearbooksManager.addYearbookInfo(
							YearbooksManager.getPersonID(), year,
							eventDescription);
				}
				System.out.println();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 关闭数据库
		DBManager.closeConnection(stmt, conn);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		YearbooksManager ybm = new YearbooksManager();
		// txt录入
		// ybm.readTxtIntoDB("/Users/ZhuangXulin/Downloads/zhouenlai.txt");
		// 读取excel
		ybm.readExcelIntoDB("/Users/ZhuangXulin/Desktop/yearbooks.xls");
	}
}
