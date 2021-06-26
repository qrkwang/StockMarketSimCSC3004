import java.awt.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;

import javax.swing.*;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

import classes.Stock;


public class UITemp {
	private JFrame frame;
    private Insets defaultInsets;
    private int currentDisplayStockId;
    private enum Country{
    	SG,
    	HK,
    	US
    }
    
	public UITemp() {
		currentDisplayStockId = -1;
		initialise();
	}
	
	public static void main(String[] args) {
		UITemp ui = new UITemp();
	}
	
	public void addLabel(JPanel panel, String label, int gridx, int gridy, Insets insets) {
		GridBagConstraints gbc = new GridBagConstraints();
		JLabel lbl = new JLabel(label);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.insets = insets;
        panel.add(lbl, gbc);
	}

	
	public void addTextField(JPanel panel, int column, int gridx, int gridy) {
		GridBagConstraints gbc = new GridBagConstraints();
        JTextField txtField = new JTextField();
        txtField.setColumns(column);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = defaultInsets;
        panel.add(txtField, gbc);
	}
	
	public void initialise() {
		frame = new JFrame("Stock");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		defaultInsets =  new Insets(0, 0, 10, 10);
		
		JPanel loginPanel = createLoginPanel();
		
		JTabbedPane homePane = createHomePane();
		
		List<Date> xData = new ArrayList<Date>();
		List<Double> openData = new ArrayList<Double>();
		List<Double> highData = new ArrayList<Double>();
		List<Double> lowData = new ArrayList<Double>();
		List<Double> closeData = new ArrayList<Double>();
		
		OHLCChart chart = new OHLCChartBuilder().width(800).height(600).title("Prices").build();
		XChartPanel<OHLCChart> ohlcChart = new XChartPanel<OHLCChart>(chart);
		JPanel chartPanel = createChartPanel(chart, ohlcChart, xData, openData, highData, lowData, closeData);
		System.out.println("Done initialised!");
		frame.getContentPane().add(homePane, BorderLayout.CENTER);
		frame.validate();
//		while(true) {
//		     try {
////				Thread.sleep(100);
////				cal.add(Calendar.DATE, 1);
////				xData.add(cal.getTime());
////				
////				double previous = data;
////				
////				data = getNewClose(data, startPrice);
////				System.out.println(", data: "+data);
////				openData.add(previous);
////				double high = getHigh(Math.max(previous, data), startPrice);
////				System.out.println(", high: "+high);
////				highData.add(high);
////				double low = getLow(Math.min(previous, data), startPrice);
////				System.out.println(", low: "+low);
////				lowData.add(low);
////				
////				closeData.add(data);
////				System.out.println("previous: "+previous+", data: "+data+", high: "+high+", low: "+low);
//				chart.updateOHLCSeries("Series", xData, openData, highData, lowData, closeData);
//				ohlcChart.repaint();
//				frame.repaint();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		}
	}
	
	public JPanel createLoginPanel() {
        frame.setBounds(100, 100, 350, 250);
		GridBagConstraints gbc = new GridBagConstraints();
		JPanel loginPanel = new JPanel();
		loginPanel.setLayout(new GridBagLayout());
		
		addLabel(loginPanel, "Username :", 0, 0, defaultInsets);

		addTextField(loginPanel, 10, 1, 0);

		addLabel(loginPanel, "Password :", 0, 1, defaultInsets);

        JTextField TxtPassword = new JPasswordField();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = defaultInsets;
        loginPanel.add(TxtPassword, gbc);

        JLabel errorLbl = new JLabel("");
        errorLbl.setForeground(Color.RED);
        errorLbl.setHorizontalAlignment(JLabel.CENTER);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = defaultInsets;
        loginPanel.add(errorLbl, gbc);
        
        JButton btnLogin = new JButton("Login");
        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = defaultInsets;
        loginPanel.add(btnLogin, gbc);
        gbc.gridwidth = 0;
        
		return loginPanel;
	}
	
	public JTabbedPane createHomePane() {
		JTabbedPane homePane = new JTabbedPane();
		JPanel dashboardPanel = new JPanel();
		JPanel SGPanel = createCountryPanel(Country.SG);
		JPanel HKPanel = createCountryPanel(Country.HK);
		JPanel USPanel = createCountryPanel(Country.US);
		homePane.add("SG Market", new JScrollPane(SGPanel));
		return homePane;
	}
	
	public JPanel createCountryPanel(Country country) {
		Insets insets = new Insets(0, 0, 10, 50);
		GridBagConstraints gbc = new GridBagConstraints();
		JPanel JSPane = new JPanel();
		JSPane.setLayout(new GridBagLayout());
		switch(country) {
			case SG: break;
			case HK: break;
			case US: break; 
		}
		addLabel(JSPane, "Company Name", 0, 0, insets);
		addLabel(JSPane, "Ticker Symbol", 1, 0, insets);
		addLabel(JSPane, "Value", 2, 0, insets);
		addLabel(JSPane, "Status", 3, 0, insets);
		
		for(int i = 0; i < 20; i++) {
			String stockName = "Stock " + i;
			int stockValue = i;
			Stock s = new Stock(i, stockName, stockName, i, true, "", LocalDateTime.now());
			addLabel(JSPane, s.getCompanyName(), 0, i+1, insets);
			addLabel(JSPane, s.getTickerSymbol(), 1, i+1, insets);
			addLabel(JSPane, s.getCurrentValue()+"", 2, i+1, insets);
			if(s.isStatus()) {
				addLabel(JSPane, "open", 3, i+1, insets);
			}else
				addLabel(JSPane, "closed", 3, i+1, insets);
			JButton submitBtn = new JButton("Trade " + stockName);
			submitBtn.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	            	currentDisplayStockId = stockValue;
	            	System.out.println("Stock value = " + stockValue);
	            }
	        });
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.gridx = 4;
	        gbc.gridy = i+1;
	        gbc.insets = defaultInsets;
	        JSPane.add(submitBtn, gbc);
		}
		return JSPane;
	}

	public JPanel createChartPanel(
			OHLCChart chart,
			XChartPanel<OHLCChart> ohlcChart,
			List<Date> xData,
			List<Double> openData,
			List<Double> highData,
			List<Double> lowData,
			List<Double> closeData) {
		
//        frame.setBounds(100, 100, 1000, 900);
//		GridBagConstraints gbc = new GridBagConstraints();
		JPanel chartPanel = new JPanel();
//		chartPanel.setLayout(new GridBagLayout());
//		// Create Chart
//		
//		// Customize Chart
//		chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
//		chart.getStyler().setLegendLayout(Styler.LegendLayout.Horizontal);
//		
//		xData = null;
//		chart.addSeries("Series", xData, openData, highData, lowData, closeData);
//		chart.getStyler().setToolTipsEnabled(true);
//		ohlcChart =  new XChartPanel<OHLCChart>(chart);
//		JPanel ohlcPanel = ohlcChart;
//
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//        gbc.gridx = 0;
//        gbc.gridy = 0;
//        gbc.insets = defaultInsets;
//		chartPanel.add(ohlcPanel, gbc);
//		
//		JTabbedPane orderPane = new JTabbedPane();
//		JPanel orderBookPanel = new JPanel();
//		JPanel openOrderPanel = new JPanel();
//		orderPane.add("Order Book", orderBookPanel);
//		orderPane.add("Open Orders", openOrderPanel);
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//        gbc.gridx = 0;
//        gbc.gridy = 1;
//        gbc.insets = defaultInsets;
//		chartPanel.add(orderPane,gbc);
		return chartPanel;
	}
}
