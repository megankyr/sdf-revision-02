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
            String id = null;
            int count = 0;
            double budget = 0.0;

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

            double price = 0;
            double spent = 0;
            double remaining = 0;

            StringBuilder productIDsBuilder = new StringBuilder();

            for (Product selectedProduct : selectedProducts) {
                System.out.println(selectedProduct);
                price = selectedProduct.getPrice();
                spent += price;
                remaining = budget - spent;
                productIDsBuilder.append(selectedProduct.getId()).append(", ");

            }

            String productIDs = productIDsBuilder.toString();
            if (productIDs.endsWith(", ")) {
                productIDs = productIDs.substring(0, productIDs.length() - 2);
            }

            out.println("request_id: " + id);
            out.println("name : Koh Yan Rong Megan");
            out.println("email: megankyr@gmail.com");
            out.println("items: " + productIDs);
            out.printf("spent: %.2f\n", spent);
            out.printf("remaining: %.2f\n", remaining);
            out.println("client_end");

            String response;
            while ((response = in.readLine()) != null){
                if (response.equals("success") || response.equals("failed")){
                    System.out.println(response);
                }
            }

        }
        
    }

    private static Product productBreakdown(BufferedReader in) throws Exception {
        String id = null, name = null, rating = null, price = null;

        String line;
        while ((line = in.readLine()) != null) {

            if (line.equals("prod_start")) {
                continue;
            }

            if (line.equals("prod_end")) {
                if (id != null && name != null && rating != null && price != null) {
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