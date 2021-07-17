package classes;

import java.io.Serializable;
import java.time.LocalDateTime;

@SuppressWarnings("serial")
public class OrderBook implements Serializable {
	private String Type;
	private int Quantity;
	private float Price;
	
	public OrderBook() {
		super();
	}
	
	public OrderBook(String type, int quantity, float price) {
		super();
		Type = type;
		Quantity = quantity;
		Price = price;
	}

	public String getType() {
		return Type;
	}

	public void setMarketPendingId(String type) {
		Type = type;
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

	@Override
	public String toString() {
		return "Type: " + Type + "\nQuantity:" + Quantity + "\nPrice: " + Price;
	}
}
