package com.dslab.receiver.Controller.RequestParam;

public class OrderItem {
    private String id;
    private int number;

    public OrderItem() {}

    public OrderItem(String id, int number) {
        this.id = id;
        this.number = number;
    }

    public String getId() {
        return this.id;
    }

    public int getNumber() {
        return this.number;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "{id: " + id + ", number: " + number + "}";
    }
}
