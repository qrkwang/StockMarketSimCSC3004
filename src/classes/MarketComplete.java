package classes;

import java.time.LocalDateTime;

public class MarketComplete {
	private int MarketCompleteId;
	private int StockId;
	private int SellerId;
	private int BuyerId;
	private int Quantity;
	private float Price;
	private LocalDateTime TransactionDate;

	public int getMarketCompleteId() {
		return MarketCompleteId;
	}

	public void setMarketCompleteId(int marketCompleteId) {
		MarketCompleteId = marketCompleteId;
	}

	public int getStockId() {
		return StockId;
	}

	public void setStockId(int stockId) {
		StockId = stockId;
	}

	public int getSellerId() {
		return SellerId;
	}

	public void setSellerId(int sellerId) {
		SellerId = sellerId;
	}

	public int getBuyerId() {
		return BuyerId;
	}

	public void setBuyerId(int buyerId) {
		BuyerId = buyerId;
	}

	public int getQuantity() {
		return Quantity;
	}

	public void setQuantity(int quantity) {
		Quantity = quantity;
	}

	public float getPrice() {
		return Price;
	}

	public void setPrice(float price) {
		Price = price;
	}

	public LocalDateTime getTransactionDate() {
		return TransactionDate;
	}

	public void setTransactionDate(LocalDateTime transactionDate) {
		TransactionDate = transactionDate;
	}

}
