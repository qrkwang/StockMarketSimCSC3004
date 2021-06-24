package classes;

import java.time.LocalDateTime;

public class MarketPending {
	private int MarketPending;
	private int StockId;
	private int SellerId;
	private int BuyerId;
	private int Quantity;
	private float Price;
	private LocalDateTime CreatedDate;

	public int getMarketPending() {
		return MarketPending;
	}

	public void setMarketPending(int marketPending) {
		MarketPending = marketPending;
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

	public LocalDateTime getCreatedDate() {
		return CreatedDate;
	}

	public void setCreatedDate(LocalDateTime createdDate) {
		CreatedDate = createdDate;
	}
}
