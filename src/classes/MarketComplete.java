package classes;

import java.io.Serializable;
import java.time.LocalDateTime;

@SuppressWarnings("serial")
public class MarketComplete implements Serializable {
	private int MarketCompleteId;
	private int StockId;
	private int SellerId;
	private int BuyerId;
	private int Quantity;
	private float Price;
	private LocalDateTime TransactionDate;
	
	public MarketComplete() {
		super();
	}
	public MarketComplete(int marketCompleteId, int stockId, int sellerId, int buyerId, int quantity, float price,
			LocalDateTime transactionDate) {
		super();
		MarketCompleteId = marketCompleteId;
		StockId = stockId;
		SellerId = sellerId;
		BuyerId = buyerId;
		Quantity = quantity;
		Price = price;
		TransactionDate = transactionDate;
	}

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

	@Override
	public String toString() {
		return "StockId: " + StockId + "\nBuyerId: " + BuyerId + "\nSellerId : " + SellerId + "\nQuantity: " + Quantity
				+ "\nPrice: " + Price;
	}

}
