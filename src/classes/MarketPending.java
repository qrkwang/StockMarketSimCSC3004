package classes;

import java.io.Serializable;
import java.time.LocalDateTime;

@SuppressWarnings("serial")
public class MarketPending implements Serializable {
	private int MarketPendingId;
	private int StockId;
	private int SellerId;
	private int BuyerId;
	private int Quantity;
	private float Price;
	private LocalDateTime CreatedDate;

	public int getMarketPendingId() {
		return MarketPendingId;
	}

	public void setMarketPendingId(int marketPendingId) {
		MarketPendingId = marketPendingId;
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

	@Override
	public String toString() {
		return "StockId: " + StockId + "\nBuyerId: " + BuyerId + "\nSellerId : " + SellerId + " Quantity:\n" + Quantity
				+ "\nPrice: " + Price;
	}
}
