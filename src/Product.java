public class Product {
    private String id;
    private String name;
    private double rating;
    private double price;

    public Product(String id, String name, double rating, double price) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getRating() {
        return rating;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", rating=" + rating +
                ", price=" + price +
                '}';
    }
}