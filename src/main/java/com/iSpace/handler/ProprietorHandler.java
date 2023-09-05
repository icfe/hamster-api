package com.iSpace.handler;

import org.dalesbred.Database;
import org.json.JSONObject;

import spark.*;

public class ProprietorHandler {

  private final Database database;

  public ProprietorHandler(Database database) {
    this.database = database;
  }

  public JSONObject deletePost(Request request, Response response) {
    var storeId = Long.parseLong(request.params(":storeId"));
    var productId = Long.parseLong(request.params(":productId"));

    database.updateUnique("DELETE FROM products " +
        "WHERE store_id = ? AND product_id = ?", storeId, productId);
    response.status(200);
    return new JSONObject();
  }
}