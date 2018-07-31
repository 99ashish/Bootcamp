package com.jda.core.StockReport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.jda.utility.QueueLinkedList;
import com.jda.utility.QueueLinkedList.Node;
import com.jda.utility.StackLinkedList;

public class StockAccount {

	private String path = "/home/bridgelabz/java-programs/Functional-Programs/ObjectOriented/src/com/jda/core/StockReport";
	private QueueLinkedList<CompanyShares> sharesOfPerson = new QueueLinkedList<>();
	private List<Stock> sharesAvailable = sharesAvailable();
	private StackLinkedList<String> companyTransactions = new StackLinkedList<>();
	private LinkedList<String> dateTransactions = new LinkedList<>();
	private Date date = new Date();

	public List<String> allFiles() {
		List<String> allFiles = new ArrayList<String>();
		File folder = new File(path);
		File[] files = folder.listFiles();
		for (File x : files) {
			allFiles.add(x.getName());
		}
		return allFiles;
	}

	public List<Stock> sharesAvailable() throws JsonParseException, JsonMappingException, IOException {
		List<Stock> sharesAvailable = StockPortfolio.readFromFile();
		return sharesAvailable;
	}

	public StockAccount(String filename) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		List<String> allFiles = allFiles();
		filename = filename.toLowerCase();
		File file = new File(path + "/" + filename);
		if (allFiles.contains(filename + ".json")) {
			String data = new String(Files.readAllBytes(Paths.get(path + "/" + filename + ".json")));
			sharesOfPerson = mapper.readValue(data, new TypeReference<ArrayList<CompanyShares>>() {
			});
			return;
		}
		file.createNewFile();
	}

	public double valueOf() {
		int amount = 0;
		// for (CompanyShares shareOfPerson : sharesOfPerson) {
		// for (Stock stockOfCompany : sharesAvailable) {
		// amount += shareOfPerson.getNumberOfShares() *
		// stockOfCompany.getPrice();
		// }
		// }
		QueueLinkedList.Node<CompanyShares> node = sharesOfPerson.head;
		while (node != null) {
			for (Stock stockOfCompany : sharesAvailable) {
				amount += node.data.getNumberOfShares() * stockOfCompany.getPrice();
			}
			node = node.next;
		}
		return amount;
	}

	public void buy(int amount, String symbol) throws JsonParseException, JsonMappingException, IOException {
		for (Stock x : sharesAvailable) {
			if ((x.getName().toLowerCase().equals(symbol.toLowerCase()))
					&& (x.getPrice() * x.getQuantity() >= amount)) {
				CompanyShares share = new CompanyShares();
				share.setSymbol(x.getName());
				share.setDate(date.toString());
				if (companyPresent(symbol) != null) {
					share = companyPresent(symbol);
					share.setNumberOfShares(share.getNumberOfShares() + (int) (amount / x.getPrice()));
					sharesOfPerson.pop(companyPresent(symbol));
					sharesOfPerson.push(share);
				} else {
					share.setNumberOfShares((int) (amount / x.getPrice()));
					sharesOfPerson.push(share);
				}
				x.setQuantity(x.getQuantity() - (int) (amount / x.getPrice()));
				companyTransactions.push(share.getSymbol());
				dateTransactions.push(date.toString());
			}
		}
	}

	public CompanyShares companyPresent(String symbol) {
		// for (CompanyShares x : sharesOfPerson) {
		// if (x.getSymbol().toLowerCase().equals(symbol.toLowerCase())) {
		// return x;
		// }
		// }
		QueueLinkedList.Node<CompanyShares> node = sharesOfPerson.head;
		while (node != null) {
			if (node.data.getSymbol().toLowerCase().equals(symbol.toLowerCase())) {
				return node.data;
			}
			node = node.next;
		}
		return null;
	}

	public void sell(int amount, String symbol) {
		boolean companyFound = false;
		QueueLinkedList.Node<CompanyShares> node = sharesOfPerson.head;
		while (node != null) {
			if (node.data.getSymbol().toLowerCase().equals(symbol.toLowerCase())) {
				companyFound = true;
				float price = 0;
				for (Stock stock : sharesAvailable) {
					if (stock.getName().toLowerCase().equals(node.data.getSymbol().toLowerCase())) {
						price = stock.getPrice();
						float value = price * node.data.getNumberOfShares();
						if (value >= amount) {
							node.data.setNumberOfShares(node.data.getNumberOfShares() - (int) (amount / price));
							node.data.setDate(date.toString());
						}
						stock.setQuantity(stock.getQuantity() + (int) (amount / price));
						break;
					}
				}
			}
			node = node.next;
		}
		if (!companyFound) {
			System.out.print("Company does not exist!");
			return;
		}
		companyTransactions.pop();
		dateTransactions.push(date.toString());
	}

	public void save(String filename) throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(sharesOfPerson);
		FileWriter file = new FileWriter(path + "/" + filename + ".json");
		file.write(json);
		file.close();
		json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(sharesAvailable);
		file = new FileWriter(path + "/stocks.json");
		file.write(json);
		file.close();
		System.out.print("\nData saved successfully!\n");
	}

	public void printReport() {

		System.out.print(
				"--------------------------------------------------------------------------------------------------------------");
		System.out.print("\nAccount Details:\n");
		int i = 1;
		QueueLinkedList.Node<CompanyShares> node = sharesOfPerson.head;
		while (node!=null) {
			System.out.print("\n");
			System.out.print(i + ". " + node.data.getSymbol() + " : " + node.data.getNumberOfShares() + "\n");
			i++;
			node = node.next;
		}

		System.out.print(
				"--------------------------------------------------------------------------------------------------------------");
		System.out.print("\nShares Available :\n\n");
		for (i = 0; i < sharesAvailable.size(); i++) {
			System.out.print(sharesAvailable.get(i).getName());
			if (i != sharesAvailable.size() - 1) {
				System.out.print(" , ");
			}
		}
		System.out.print("\n\n");
		System.out.print(
				"--------------------------------------------------------------------------------------------------------------");
	}
}
