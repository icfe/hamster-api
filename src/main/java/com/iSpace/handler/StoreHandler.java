package com.iSpace.handler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import org.dalesbred.Database;
import org.json.*;

import spark.*;

public class StoreHandler {

  private final Database database;

  public StoreHandler(Database database) {
    this.database = database;
  }

  public JSONObject createStore(Request request, Response response) {
    var json = new JSONObject(request.body());
    var storeName = json.getString("name");
    if (storeName.length() > 255) {
      throw new IllegalArgumentException("store name is too long");
    }
    var owner = json.getString("owner");
    if (!owner.matches("[a-zA-Z][a-zA-Z0-9]{1,29}")) {
      throw new IllegalArgumentException("invalid username");
    }

    return database.withTransaction(tx -> {
      var storeId = database.findUniqueLong(
          "SELECT NEXT VALUE FOR store_id_seq;");

      database.updateUnique(
          "INSERT INTO stores(store_id, name, owner) " +
              "VALUES(?, ?, ?);", storeId, storeName, owner);

      response.status(201);
      response.header("Location", "/stores/" + storeId);

      return new JSONObject()
          .put("name", storeName) .put("uri", "/stores/" + storeId);

    });
  }

  // Additional REST API endpoints not covered in the book:
  public JSONObject postProduct(Request request, Response response) {
    var storeId = Long.parseLong(request.params(":storeId"));
    var json = new JSONObject(request.body());

    var product = json.getString("product");
  if (product.length() > 1024) {
    throw new IllegalArgumentException("product name is too long");
  }

    var product_price = json.getString("product_price");
    if (!product_price.matches("[0-9]+")) {
      throw new IllegalArgumentException("invalid price");
  }
    var quantity = json.getString("quantity");
    if (!quantity.matches("[0-9]+")) {
      throw new IllegalArgumentException("invalid quantity");
  }
   
    return database.withTransaction(tx -> {
      var productId = database.findUniqueLong(
          "SELECT NEXT VALUE FOR product_id_seq;");
      database.updateUnique(
          "INSERT INTO products(product_id, product_id, post_time," +
              "quantity, product_desc) " +
              "VALUES(?, ?, current_timestamp, ?, ?)",
          storeId, productId, quantity, product);

      response.status(201);
      var uri = "/stores/" + storeId + "/products/" + productId;
      response.header("Location", uri);
      return new JSONObject().put("uri", uri);
    });
  }

  public product readProduct(Request request, Response response) {
    var storeId = Long.parseLong(request.params(":storeId"));
    var productId = Long.parseLong(request.params(":productId"));

    var product = database.findUnique(product.class,
        "SELECT product_id, product_id, quantity, post_time, product_desc " +
            "FROM products WHERE product_id = ? AND product_id = ?",
        productId, storeId);

    response.status(200);
    return product;
  }

  public JSONArray findproducts(Request request, Response response) {
    var since = Instant.now().minus(1, ChronoUnit.DAYS);
    if (request.queryParams("since") != null) {
      since = Instant.parse(request.queryParams("since"));
    }
    var storeId = Long.parseLong(request.params(":storeId"));

    var products = database.findAll(Long.class,
        "SELECT product_id FROM products " +
            "WHERE product_id = ? AND post_time >= ?;",
        storeId, since);

    response.status(200);
    return new JSONArray(products.stream()
        .map(productId -> "/stores/" + storeId + "/products/" + productId)
        .collect(Collectors.toList()));
  }

  public static class product {
    private final long storeId;
    private final long productId;
    private final String quantity;
    private final Instant time;
    private final String product;

    public product(long storeId, long productId, String quantity,
        Instant time, String product) {
      this.storeId = storeId;
      this.productId = productId;
      this.quantity = quantity;
      this.time = time;
      this.product = product;
    }
    @Override
    public String toString() {
      JSONObject msg = new JSONObject();
      msg.put("uri",
          "/stores/" + storeId + "/products/" + productId);
      msg.put("quantity", quantity);
      msg.put("time", time.toString());
      msg.put("product", product);
      return msg.toString();
    }
  }
}