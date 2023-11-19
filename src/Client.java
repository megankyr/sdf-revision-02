import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Client {
    public static void main(String[] args) throws Exception {
        String host;
        int port;
        if (args.length == 1) {
            host = "localhost";
            port = Integer.parseInt(args[0]);
        } else if (args.length == 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        } else {
            host = "localhost";
            port = 3000;
        }

        try (Socket clientSocket = new Socket(host, port);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            System.out.println("Connected to server");

            List<Product> productList = new ArrayList<>();
            double budget = 0.0;
            int count = 0;
            String id = null;

            String line;
            while ((line = in.readLine()) != null) {

                if (line.startsWith("request_id:")) {
                    id = line.substring(12);
                    System.out.println("Request id is " + id);
                }

                if (line.startsWith("item_count:")) {
                    count = Integer.parseInt(line.substring(12));
                    System.out.println("Item count is " + count);
                }

                if (line.startsWith("budget:")) {
                    budget = Double.parseDouble(line.substring(8));
                    System.out.println("Item budget is " + budget);
                }

                if (line.equals("prod_start")) {
                    Product product = productBreakdown(in);
                    productList.add(product);
                    if (productList.size() == count) {
                        break;
                    }

                }

            }

            Collections.sort(productList, Comparator.comparing(Product::getRating, Comparator.reverseOrder())
            .thenComparing(Product::getPrice, Comparator.reverseOrder()));

            System.out.println("Sorted Products:");
            for (Product sortedProduct : productList) {
                System.out.println(sortedProduct);
            }

            List<Product> selectedProducts = selectProducts(productList, budget);

            System.out.println("Selected Products:");
            for (Product selectedProduct : selectedProducts) {
                System.out.println(selectedProduct);
            }

        }
    }

    private static Product productBreakdown(BufferedReader in) throws Exception {
        String id = null, name = null, rating = null, price = null;

        String line;
        while ((line = in.readLine()) != null) {
            System.out.println("Processing line: " + line);

            if (line.equals("prod_start")) {
                continue;
            }

            if (line.equals("prod_end")) {
                if (id != null && name != null && rating != null && price != null) {
                    // If all fields are present, create and return the product
                    return new Product(id, name, Double.parseDouble(rating), Double.parseDouble(price));
                }
            }

            String[] directive = line.split(":");
            if (directive.length == 2) {
                String field = directive[0].trim();
                String value = directive[1].trim();

                switch (field) {
                    case "prod_id":
                        id = value;
                        break;
                    case "title":
                        name = value;
                        break;
                    case "price":
                        price = value;
                        break;
                    case "rating":
                        rating = value;
                        break;
                }
            }
        }

        return null;
    }

    private static List<Product> selectProducts(List<Product> productList, double budget) {
        List<Product> selectedProducts = new ArrayList<>();
        double remainingBudget = budget;

        for (Product product : productList) {
            if (product.getPrice() <= remainingBudget) {
                selectedProducts.add(product);
                remainingBudget -= product.getPrice();
            }
        }

        return selectedProducts;
    }

}