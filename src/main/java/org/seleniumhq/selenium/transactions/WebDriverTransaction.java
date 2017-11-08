package org.seleniumhq.selenium.transactions;

/**
 * Created by paul on 11/6/17.
 */
public class WebDriverTransaction {
    private String name;
    private String id;
    public WebDriverTransaction(String id, String name) {
        this.name = name;
        this.id = id;
    }
    public WebDriverTransaction(String name) {
        this.name = name;
    }
    public String getName() { return this.name; }
    public String getId() { return this.id; }
}
