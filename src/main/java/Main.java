import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static List<Employee> parseCSV (String[] arr, String fileName) {
        try (CSVReader csvReader= new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(arr);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();
            return csv.parse();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static String listToJson (List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Type listType = new TypeToken<List<Employee>>() {}.getType();
        return gson.toJson(list, listType);
    }

    public static void writeString (String json, String path) {
        try (PrintWriter out = new PrintWriter(new FileWriter(path))) {
            out.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Employee> parseXML (String path) {
        List<Employee> list = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(path));
            Node root = document.getDocumentElement();
            list = read(root);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public static List<Employee> read (Node node) {
        List<Employee> result = new ArrayList<>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("employee")) {
                String[] arr = child.getTextContent()
                        .trim()
                        .replaceAll("\\s+", ",")
                        .split(",");
                Employee employee = new Employee(Long.parseLong(arr[0]), arr[1], arr[2], arr[3], Integer.parseInt(arr[4]));
                result.add(employee);
            }
            read(child);
        }
        return result;
    }

    //Сделал 2 варианта метода. Оставил, который короче. Второй закомментил. Работают оба
    public static String readString(String nameFile) throws IOException {
        return new String(Files.readAllBytes(Paths.get(nameFile)));
        /*try (BufferedReader br = new BufferedReader(new FileReader(nameFile))) {
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = br.readLine()) != null) {
                sb.append(s);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;*/
    }

    public static List<Employee> jsonToList (String json) {
        Type listType = new TypeToken<List<Employee>>() {}.getType();
        return new Gson().fromJson(json, listType);
    }

    public static void main(String[] args) throws IOException {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";

        List<Employee> list = parseCSV(columnMapping, fileName);
        String json = listToJson(list);
        writeString(json, "data.json");

        List<Employee> list2 = parseXML("data.xml");
        String json2 = listToJson(list2);
        writeString(json2, "data2.json");

        String str3 = readString("data.json");
        List<Employee> list3 = jsonToList(str3);
        for (Employee employee : list3) {
            System.out.println(employee);
        }
    }
}
