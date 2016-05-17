package jp.co.iccom.matsuyama_minori.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;


public class SalesAggregate {
	public static void main(String[] args) {

		if(args.length != 1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		HashMap<String, String> shopMap = new HashMap<String, String>();
		HashMap<String, Long> shopTotal = new HashMap<String, Long>();
		if(!inputDefinition(args[0], shopMap, shopTotal, "branch.lst", "支店", "^\\d{3}$")){
			return;
		};

		HashMap<String, String> productMap = new HashMap<String, String>();
		HashMap<String, Long> productTotal = new HashMap<String, Long>();
		if(!inputDefinition(args[0], productMap, productTotal, "commodity.lst", "商品", "^\\w{8}$")){
			return;
		};


		File sales = new File(args[0]);
		String[] salesFile = sales.list(new RcdFilter());
		Arrays.sort(salesFile);
		BufferedReader br = null;

		String shopCode = null;
		String productCode = null;
		Long sale;

		for(int i=0; i<salesFile.length; ++i) {
			ArrayList<String> elementList = new ArrayList<String>();

			int minIndex = salesFile[0].indexOf(".");
			int maxIndex = salesFile[salesFile.length - 1].indexOf(".");

			String characterMin = salesFile[0].substring(0, minIndex);
			int nameMin = Integer.parseInt(characterMin);

			String characterMax = salesFile[salesFile.length - 1].substring(0, maxIndex);
			int nameMax = Integer.parseInt(characterMax);


			if(nameMin + salesFile.length - 1 != nameMax) {
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}


			try {
				File inputFile = new File (args[0], salesFile[i]);
				FileReader fr = new FileReader (inputFile);
				br = new BufferedReader (fr);
				String Line;
				while((Line = br.readLine()) != null) {
					elementList.add(Line);
				}

				if(elementList.size() != 3) {
					System.out.println(salesFile[i] + "のフォーマットが不正です");
					return ;
				}

				shopCode = elementList.get(0);
				productCode = elementList.get(1);
				sale = Long.parseLong(elementList.get(2));

				if(!aggregate(shopMap, shopCode, salesFile[i], "支店", shopTotal, sale)){
					return;
				};
				if(!aggregate(productMap, productCode, salesFile[i], "商品", productTotal, sale)){
					return;
				};

			} catch(Exception e) {
				System.out.println("予期せぬエラーが発生しました");
				return;

			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						System.out.println("予期せぬエラーが発生しました");
						return;
					}
				}
			}
		}

		if(!outputAggregate(shopTotal, args[0], "branch.out", shopMap)){
			return;
		};
		if(!outputAggregate(productTotal, args[0], "commodity.out", productMap)){
			return;
		};
	}

	private static boolean inputDefinition(String dir, HashMap<String, String> definition,
			HashMap<String, Long> total, String name, String category, String regex){
		BufferedReader br = null;
		try {
			File inputFile = new File (dir, name);
			FileReader fr = new FileReader (inputFile);
			br = new BufferedReader (fr);
			String Line;
			while ((Line = br.readLine())!= null) {
				String[] items = Line.split(",");

				if(items.length != 2) {
					System.out.println(category + "定義ファイルのフォーマットが不正です");
					return false;
				}

				if(!items[0].matches(regex)) {
					System.out.println(category + "定義ファイルのフォーマットが不正です");
					return false;
				}

				definition.put(items[0], items[1]);
				total.put(items[0], 0L);
			}

		} catch(IOException e) {
			System.out.println(category + "定義ファイルが存在しません");
			return false;

		} catch(Exception e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;

		} finally {
			if(br != null) {
				try {
					br.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return false;
				}
			}
		}
		return true;
	}

	private static boolean aggregate(HashMap<String, String> definition, String code,
			String file, String category, HashMap<String, Long> total, Long proceeds){
		if(!definition.containsKey(code)) {
			System.out.println(file + "の" + category + "コードが不正です");
			return false;
		}

		total.put(code, (total.get(code) + proceeds));
		if(String.valueOf(total.get(code)).length() > 10) {
			System.out.println("合計金額が10桁を超えました");
			return false;
		}
		return true;
	}

	private static boolean outputAggregate(HashMap<String, Long> total, String dir, String name,
			HashMap<String, String> definition){
		BufferedWriter bw = null;

		try {
			List<Entry<String, Long>> descendingList = new ArrayList<Entry<String, Long>>(total.entrySet());
			Collections.sort(descendingList, new Comparator<Entry<String, Long>>() {
				@Override
				public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
					return o2.getValue().compareTo(o1.getValue());
				}

			});

			File outputFile = new File (dir, name);
			FileWriter fw = new FileWriter (outputFile);
			bw = new BufferedWriter (fw);
			for(Entry<String,Long> totalOrder : descendingList) {
				bw.write(totalOrder.getKey() + "," + definition.get(totalOrder.getKey()) + "," + totalOrder.getValue() + "\n");
			}

		} catch(IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		} finally {
			if(bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return false;
				}
			}
		}
		return true;
	}
}

class RcdFilter implements FilenameFilter {
    public boolean accept(File SalesAggregate, String fileName) {
    	String regexp = "^\\d{8}.rcd$";
    	File rcdFile =new File (SalesAggregate, fileName);

    	if(fileName.matches(regexp) && rcdFile.isFile()) { return true; }
    	return false;
    }
}
