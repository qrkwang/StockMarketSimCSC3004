package classes;

import java.time.LocalDateTime;

public class Stock {
	private int StockId;
	private String CompanyName;
	private String TickerSymbol;
	private float CurrentValue;
	private boolean Status;
	private String Timezone;
	private LocalDateTime CreatedDate;

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

	public float getCurrentValue() {
		return CurrentValue;
	}

	public void setCurrentValue(float currentValue) {
		CurrentValue = currentValue;
	}

	public boolean isStatus() {
		return Status;
	}

	public void setStatus(boolean status) {
		Status = status;
	}

	public String getTimezone() {
		return Timezone;
	}

	public void setTimezone(String timezone) {
		Timezone = timezone;
	}

	public LocalDateTime getCreatedDate() {
		return CreatedDate;
	}

	public void setCreatedDate(LocalDateTime createdDate) {
		CreatedDate = createdDate;
	}
}
