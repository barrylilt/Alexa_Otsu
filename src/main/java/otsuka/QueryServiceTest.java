package otsuka;

import org.apache.commons.lang3.StringUtils;

import otsuka.ConnectionUtil;

public class QueryServiceTest {

	private static ConnectionUtil util = null;

	// Create Dynamic Query for Total Trade Spend for year
	private static String executeTotalTradeSpendQuery(String year) {

		// base Query
		String totalTradeSpendQuery = "select CONVERT(decimal(15), sum(TotalTradeSpendSellOut)) "
				+ "as 'Total Trade Spend' , datepart(yyyy,[PromotionStartDate])  from  "
				+ "[dbo].[VIEW_TABLEAU_PEA_DEMO] ";

		// if year is not empty add year in where condition
		if (StringUtils.isNotEmpty(year)) {
			totalTradeSpendQuery += "where datepart(yyyy,[PromotionStartDate])='2016' "
					+ "group by datepart(yyyy,[PromotionStartDate])";
		}

		System.out.println("Total Trade Spend Query :: " + totalTradeSpendQuery);

		String totalTradeSpend = util.executeQuery(totalTradeSpendQuery);

		return totalTradeSpend;

	}

	// Create Dynamic Query for Total Gross Profit based on brand
	private static String executeTotalGrossProfitQuery(String brand) {

		// base Query
		String totalGrossProfitQuery = "select CONVERT(decimal(15), sum(GrossProfitSellOut)) as "
				+ "'Total Gross Profit', ProdLevel4Name as 'Brand' from  [dbo].[VIEW_TABLEAU_PEA_DEMO] ";

		// if brand is not empty add brand in where condition
		if (StringUtils.isNotEmpty(brand)) {
			totalGrossProfitQuery += "where ProdLevel4Name ='" + brand + "' group by ProdLevel4Name";
		}

		System.out.println("Total Gross Profit Query :: " + totalGrossProfitQuery);

		String totalGrossProfit = util.executeQuery(totalGrossProfitQuery);

		return totalGrossProfit;

	}

	// Create Dynamic Query for Total ROI based on customer
	private static String executeTotalROIQuery(String customer) {

		// base Query
		String totalROIQuery = "select CONVERT(decimal(4,1), sum(GrossProfitSellOut)/sum(TotalTradeSpendSellOut) * 100) "
				+ "as 'Total ROI', CustCustomerName as 'Customer' from  [dbo].[VIEW_TABLEAU_PEA_DEMO] ";

		// if customer is not empty add customer in where condition
		if (StringUtils.isNotEmpty(customer)) {
			totalROIQuery += "where CustCustomerName ='" + customer + "' group by CustCustomerName";
		}

		System.out.println("Total ROI Query :: " + totalROIQuery);

		String totalROI = util.executeQuery(totalROIQuery);

		return totalROI;

	}

	public static void main1(String[] args) {

		String status = "Completed"; 

		String phase = "Phase 2";

		util = new ConnectionUtil();

		String totalStudiesQuery = "select count(title) FROM oct where Phases='" + phase + "'";

		String totalStudiesCount = util.executeQuery(totalStudiesQuery);

		String recruitmentQuery = "select count(*) from oct where Recruitment='" + status + "'";

		String recruitmentCount = util.executeQuery(recruitmentQuery);

		System.out.println("Recruitment Res:- " + recruitmentCount + " Total Studies Query:- " + totalStudiesCount);

		// util.closeConnection();

	}
}
