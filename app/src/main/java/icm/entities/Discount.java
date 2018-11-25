package icm.entities;

public class Discount {

    private static int discountID=0, storeID;
    private String title, description, storeName, value;

    public Discount (){}

    public Discount(int storeID, String title, String description, String storeName, String value) {
        this.discountID += 1;
        this.storeID = storeID;
        this.title = title;
        this.description = description;
        this.storeName = storeName;
        this.value = value;
    }

    public int getDiscountID() {
        return discountID;
    }

    public int getStoreID() {
        return storeID;
    }

    public void setStoreID(int storeID) {
        this.storeID = storeID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
