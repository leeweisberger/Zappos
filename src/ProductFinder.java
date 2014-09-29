import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class ProductFinder {
	private static ArrayList<String> combination;
	public static final int NUMBER_OF_COMBINATIONS = 7;
	
	public static void main(String[] args) throws IOException {
		ArrayList<ArrayList<String>> productList = new ArrayList<ArrayList<String>>();
		int[] userInput = getUserInput();
		while(productList.size()!=NUMBER_OF_COMBINATIONS){
			combination = new ArrayList<String>();
			getProductCombination(userInput[0], userInput[1]);
			productList.add(combination);
		}
		System.out.println("---------" + NUMBER_OF_COMBINATIONS + " combinations of the given price/quantity"+"---------");
		for(ArrayList<String> combination : productList)
			System.out.println(combination);
	}

	private static void getProductCombination(int price, int quantity)
			throws IOException {
		int numberOfResults=0;
		JSONObject JSONResponse = null;
		int price1 = 0;
		//break if we find a price that has viable products
		while(numberOfResults==0){
			price1 = getRandomPrice(price, quantity);
			JSONResponse = ProductFinder.getProductsFromPrices("%22"+price1+".0%22");
			numberOfResults = Integer.valueOf((String) JSONResponse.get("currentResultCount"));
			//If we are on the last product and can't find any viable matches, increment
			// or decrement the original price by 1 and look again!
			if(numberOfResults==0 && quantity==1){
				price+= price>100 ? -1 : 1;
			}
		}
		
		JSONArray JSONProducts = (JSONArray) JSONResponse.get("results");
		combination.add((String) ((JSONObject)JSONProducts.get(new Random().nextInt(numberOfResults))).get("productName"));
		combination.add((String) ((JSONObject)JSONProducts.get(new Random().nextInt(numberOfResults))).get("price"));

		price-=Integer.valueOf(price1);
		quantity--;
		if(quantity!=0)
			getProductCombination(price, quantity);
	}

	private static int[] getUserInput(){
		int price;
		int quantity;
		while(true){
			Scanner scanner = new Scanner (System.in);
			System.out.print("Integer Total Price: ");  
			price = scanner.nextInt();
			System.out.print("Number of Products: ");  
			quantity = scanner.nextInt();
			if(quantity*5>=price)
				System.out.println("Sorry! You asked us for too small of a price and/or too many products!");
			else if(price/quantity>1000)
				System.out.println("Sorry! You asked us for too high of a price and/or too few products!");
			else
				break;
		}
		return new int[] {price, quantity};
	}

	private static JSONObject getProductsFromPrices(String prices) throws IOException {
		URL obj = new URL("http://api.zappos.com/Search?limit=100&filters={%22price%22:["+prices+"]}&key=52ddafbe3ee659bad97fcce7c53592916a6bfd73");
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	
		BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
	
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		return (JSONObject) JSONValue.parse(response.toString());
	}

	private static int getRandomPrice(int price, int quantity) {
		int price1;
		//If we are on the last product, we want to get to the exact amount!
		if(quantity==1)
			price1=price;
		else
			price1 = new Random().nextInt(price-5*quantity);
		return price1;
	}

}
