package com.devsuperior.dscommerce.controllers;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.devsuperior.dscommerce.tests.TokenUtil;

import io.restassured.http.ContentType;

public class OrderControllerRA {
	
	private String adminUsername, adminPassword, adminOnlyUsername, adminOnlyPassword, clientUsername, clientPassword;
	private String adminToken, clientToken, adminOnlyToken, invalidToken;
	
	private Long existingId, nonExistingId, otherId;
	
	private Map<String, List<Map<String, Object>>> postOrderInstance;
	
	@BeforeEach
	private void setUp() {
		baseURI = "http://localhost:8080";
		
		existingId = 1L;
		nonExistingId = 100L;
		otherId = 2L;

		adminUsername = "alex@gmail.com";
		adminPassword = "123456";
		adminOnlyUsername = "ana@gmail.com";
		adminOnlyPassword = "123456";
		clientUsername = "maria@gmail.com";
		clientPassword = "123456";
		
		adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
		adminOnlyToken = TokenUtil.obtainAccessToken(adminOnlyUsername, adminOnlyPassword);
		clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
		invalidToken = adminToken + "xpto";
		
		Map<String, Object> item1 = new HashMap<>();
		item1.put("productId", 1);
		item1.put("quantity", 2);
		
		Map<String, Object> item2 = new HashMap<>();
		item2.put("productId", 5);
		item2.put("quantity", 1);
		
		List<Map<String,Object>> itemInstances = new ArrayList<>();
		itemInstances.add(item1);
		itemInstances.add(item2);
		
		postOrderInstance = new HashMap<>();
		postOrderInstance.put("items", itemInstances);
	}
	
	@Test
	public void findByIdShouldReturnOrderWhenIdExistsAndAdminLogged() {
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.accept(ContentType.JSON)
		.when()
			.get("/orders/{id}", existingId)
		.then()
			.statusCode(200)
				.body("id", is(1))
				.body("moment", equalTo("2022-07-25T13:00:00Z"))
				.body("status", equalTo("PAID"))
					.body("client.name", equalTo("Maria Brown"))
					.body("payment.moment", equalTo("2022-07-25T15:00:00Z"))
					.body("items.name", hasItems("The Lord of the Rings", "Macbook Pro"))
				.body("total", is(1431.0F));
	}
	
	@Test
	public void findByIdShouldReturnOrderWhenIdExistsAndClientLogged() {
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + clientToken)
			.accept(ContentType.JSON)
		.when()
			.get("/orders/{id}", existingId)
		.then()
			.statusCode(200)
				.body("id", is(1))
				.body("moment", equalTo("2022-07-25T13:00:00Z"))
				.body("status", equalTo("PAID"))
					.body("client.name", equalTo("Maria Brown"))
					.body("payment.moment", equalTo("2022-07-25T15:00:00Z"))
					.body("items.name", hasItems("The Lord of the Rings", "Macbook Pro"))
				.body("total", is(1431.0F));
	}
	
	@Test
	public void findByIdShouldReturnForbiddenWhenIdExistsAndClientLoggedAndOrderDoesNotBelongUser() {
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + clientToken)
			.accept(ContentType.JSON)
		.when()
			.get("/orders/{id}", otherId)
		.then()
			.statusCode(403)
				.body("error", equalTo("Access denied. Should be self or admin"));
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExistAndAdminLogged() {
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.accept(ContentType.JSON)
		.when()
			.get("/orders/{id}", nonExistingId)
		.then()
			.statusCode(404)
				.body("error", equalTo("Recurso não encontrado"));
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExistAndClientLogged() {
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + clientToken)
			.accept(ContentType.JSON)
		.when()
			.get("/orders/{id}", nonExistingId)
		.then()
			.statusCode(404)
				.body("error", equalTo("Recurso não encontrado"));
	}
	
	@Test
	public void findByIdShouldReturnUnauthorizedWhenInvalidToken() {
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + invalidToken)
			.accept(ContentType.JSON)
		.when()
			.get("/orders/{id}", existingId)
		.then()
			.statusCode(401);
	}
	
	@Test
	public void insertShouldReturnOrderCreatedWhenClientLogged() {
		
		JSONObject newOrder = new JSONObject(postOrderInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + clientToken)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(newOrder)
		.when()
			.post("/orders")
		.then()
			.statusCode(201)
			.body("status", equalTo("WAITING_PAYMENT"))
			.body("client.name", equalTo("Maria Brown"))
			.body("items.name", hasItems("The Lord of the Rings", "Rails for Dummies"))
			.body("total", is(281.99F));
	}

	@Test
	public void insertShouldReturnUnprocessableEntityWhenClientLoggedAndOrderHasNoItem() {
		postOrderInstance.put("items", null);
		
		JSONObject newOrder = new JSONObject(postOrderInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + clientToken)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(newOrder)
		.when()
			.post("/orders")
		.then()
			.statusCode(422)
			.body("errors.message[0]", equalTo("Deve ter pelo menos um item"));
	}
	
	@Test
	public void insertShouldReturnForbiddenWhenAdminLogged() {
		
		JSONObject newOrder = new JSONObject(postOrderInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminOnlyToken)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(newOrder)
		.when()
			.post("/orders")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void insertShouldReturnUnauthorizedWhenInvalidToken() {
		
		JSONObject newOrder = new JSONObject(postOrderInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + invalidToken)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(newOrder)
		.when()
			.post("/orders")
		.then()
			.statusCode(401);
	}

}
