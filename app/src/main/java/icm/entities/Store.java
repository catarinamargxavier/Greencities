package icm.entities;

import java.util.ArrayList;
import java.util.List;

public class Store {

    private String name;
    private List<Discount> discount = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Discount> getDiscount() {
        return discount;
    }

    public void setDiscount(List<Discount> discount) {
        this.discount = discount;
    }

}
