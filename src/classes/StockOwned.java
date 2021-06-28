package classes;

import java.io.Serializable;

@SuppressWarnings("serial")
public class StockOwned implements Serializable {
	private int StockId;
	private String CompanyName;
	private String TickerSymbol;
	private int Quantity;
	private float avgPrice;

	public StockOwned() {
		super();
	}

	public StockOwned(int stockId, String companyName, String tickerSymbol, int quantity, int avgPrice) {
		super();
		this.StockId = stockId;
		this.CompanyName = companyName;
		this.TickerSymbol = tickerSymbol;
		this.Quantity = quantity;
		this.avgPrice = avgPrice;
	}

	public int getStockId() {
		return StockId;
	}

	public void setStockId(int stockId) {
		StockId = stockId;
	}

	public String getCompanyName() {
		return CompanyName;
	}

	public void setCompanyName(String companyName) {
		CompanyName = companyName;
	}

	public String getTickerSymbol() {
		return TickerSymbol;
	}

	public void setTickerSymbol(String tickerSymbol) {
		TickerSymbol = tickerSymbol;
	}

	public int getQuantity() {
		return Quantity;
	}

	public void setQuantity(int quantity) {
		Quantity = quantity;
	}

	public float getAvgPrice() {
		return avgPrice;
	}

	public void setAvgPrice(float avgPrice) {
		this.avgPrice = avgPrice;
	}

	@Override
	public String toString() {
		return "StockId: " + StockId + " CompanyName: " + CompanyName;
	}

}
