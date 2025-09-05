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

public class ProductControllerRA {
	
	private String adminUsername, adminPassword, clientUsername, clientPassword;
	private String adminToken, clientToken, invalidToken;
	
	private Long existingId, nonExistingId, dependentId;
	private String productName;
	
	private Map<String, Object> postProductInstance;
	private Map<String, Object> putProductInstance;
	
	@BeforeEach
	private void setUp() {
		baseURI = "http://localhost:8080"; //endereço da api que eu quero testar os endpoints, seria o projeto dscommerce-jacoco-tests

		adminUsername = "alex@gmail.com";
		adminPassword = "123456";
		clientUsername = "maria@gmail.com";
		clientPassword = "123456";
		
		adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
		clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
		invalidToken = adminToken + "xpto";
		
		productName = "Macbook";
		
		postProductInstance = new HashMap<>();
		postProductInstance.put("name", "Meu produto novo");
		postProductInstance.put("description", "Lorem ipsum, dolor sit amet consectetur adipisicing elit. Qui ad, adipisci illum ipsam velit et odit eaque reprehenderit ex maxime delectus dolore labore, quisquam quae tempora natus esse aliquam veniam doloremque quam minima culpa alias maiores commodi. Perferendis enim");
		postProductInstance.put("imgUrl", "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
		postProductInstance.put("price", 50.0);
		
		putProductInstance = new HashMap<>();
		putProductInstance.put("name", "Produto atualizado");
		putProductInstance.put("description", "Lorem ipsum, dolor sit amet consectetur adipisicing elit. Qui ad, adipisci illum ipsam velit et odit eaque reprehenderit ex maxime delectus dolore labore, quisquam quae tempora natus esse aliquam veniam doloremque quam minima culpa alias maiores commodi. Perferendis enim");
		putProductInstance.put("imgUrl", "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
		putProductInstance.put("price", 200.0);
		
		List<Map<String, Object>> categories = new ArrayList<>();
		
		Map<String, Object> category1 = new HashMap<>();
		category1.put("id", 2);
		
		Map<String, Object> category2 = new HashMap<>();
		category2.put("id", 3);
		
		categories.add(category1);
		categories.add(category2);
		
		postProductInstance.put("categories", categories);
		putProductInstance.put("categories", categories);
	}
	
	@Test
	public void findByIdShouldReturnProductDTOWhenIdExists() {
		existingId = 2L;
		
		given()
			.get("/products/{id}", existingId)
			.then()
				.statusCode(200)
				.body("id", is(2))
				.body("name", equalTo("Smart TV"))
				.body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/2-big.jpg"))
				.body("price", is(2190.0F))
					.body("categories.id", hasItems(2, 3))
					.body("categories.name", hasItems("Eletrônicos", "Computadores"));
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() {
		nonExistingId = 100L;
		
		given()
			.get("/products/{id}", nonExistingId)
			.then()
				.statusCode(404)
				.body("error", equalTo("Recurso não encontrado"));
	}
	
	@Test
	public void findAllShouldReturnPageProductsWhenProductNameIsEmpty() {
		
		given()
			.get("/products")
			.then()
				.statusCode(200)
				.body("content.name", hasItems("Macbook Pro", "PC Gamer Tera"));
	}
	
	@Test
	public void findAllShouldReturnPageProductsWhenProductNameIsNotEmpty() {
		
		given()
			.get("/products?name={productName}", productName)
			.then()
				.statusCode(200)
				.body("content.id[0]", is(3))
				.body("content.name[0]", equalTo("Macbook Pro"))
				.body("content.price[0]", is(1250.0F))
				.body("content.imgUrl[0]", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/3-big.jpg"));
	}
	
	@Test
	public void findAllShouldReturnPageProductsWhenProductPriceIsGreaterThen2000() {
		
		given()
			.get("/products")
			.then()
				.statusCode(200)
				.body("content.findAll { it.price > 2000 }.name ", hasItems("Smart TV", "PC Gamer Weed"));
	}
	
	@Test
	public void insertShouldReturnProductCreatedWhenAdminLogged() {
		JSONObject newProduct = new JSONObject(postProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
				.when()
					.post("/products")
						.then()
							.statusCode(201)
							.body("name", equalTo("Meu produto novo"))
							.body("price", is(50.0F))
							.body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"))
							.body("categories.id", hasItems(2, 3));
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndInvalidName() {
		postProductInstance.put("name", "Me");
		
		JSONObject newProduct = new JSONObject(postProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
				.when()
					.post("/products")
						.then()
							.statusCode(422)
							.body("errors.message[0]", equalTo("Nome precisar ter de 3 a 80 caracteres"));
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndInvalidDescription() {
		postProductInstance.put("description", "Lorem");
		
		JSONObject newProduct = new JSONObject(postProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
				.when()
					.post("/products")
						.then()
							.statusCode(422)
							.body("errors.message[0]", equalTo("Descrição precisa ter no mínimo 10 caracteres"));
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndPriceIsNegative() {
		postProductInstance.put("price", -50.0);
		
		JSONObject newProduct = new JSONObject(postProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
				.when()
					.post("/products")
						.then()
							.statusCode(422)
							.body("errors.message[0]", equalTo("O preço deve ser positivo"));
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndPriceIsZero() {
		postProductInstance.put("price", 0.0);
		
		JSONObject newProduct = new JSONObject(postProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
				.when()
					.post("/products")
						.then()
							.statusCode(422)
							.body("errors.message[0]", equalTo("O preço deve ser positivo"));
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndProductHasNoCategory() {
		postProductInstance.put("categories", null);
		
		JSONObject newProduct = new JSONObject(postProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
				.when()
					.post("/products")
						.then()
							.statusCode(422)
							.body("errors.message[0]", equalTo("Deve ter pelo menos uma categoria"));
	}
	
	@Test
	public void insertShouldReturnForbiddenWhenClientLogged() {
		JSONObject newProduct = new JSONObject(postProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + clientToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
				.when()
					.post("/products")
						.then()
							.statusCode(403);
	}
	
	@Test
	public void insertShouldReturnUnauthorizedWhenInvalidToken() {
		JSONObject newProduct = new JSONObject(postProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + invalidToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
				.when()
					.post("/products")
						.then()
							.statusCode(401);
	}
	
	@Test
	public void updateShouldReturnProductWhenIdExistsAndAdminLogged() {
		existingId = 10L;
		
		JSONObject newProduct = new JSONObject(putProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(newProduct)
				.when()
					.put("/products/{id}", existingId)
						.then()
							.statusCode(200)
							.body("name", equalTo("Produto atualizado"))
							.body("price", is(200.0f))
							.body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"))
							.body("categories.id", hasItems(2, 3))
							.body("categories.name", hasItems("Eletrônicos", "Computadores"));
	}
	
	@Test
	public void updateShouldReturnNotFoundWhenIdDoesNotExistAndAdminLogged() {
		nonExistingId = 100L;
		
		JSONObject newProduct = new JSONObject(putProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(newProduct)
				.when()
					.put("/products/{id}", nonExistingId)
						.then()
							.statusCode(404)
							.body("status", equalTo(404))
							.body("error", equalTo("Recurso não encontrado"));
	}
	
	@Test
	public void updateShouldReturnUnprocessableEntityWhenIdExistsAndAdminLoggedAndInvalidName() {
		putProductInstance.put("name", "Pr");
		
		existingId = 10L;
		
		JSONObject newProduct = new JSONObject(putProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(newProduct)
				.when()
					.put("/products/{id}", existingId)
						.then()
							.statusCode(422)
							.body("status", equalTo(422))
							.body("errors.message[0]", equalTo("Nome precisar ter de 3 a 80 caracteres"));
	}
	
	@Test
	public void updateShouldReturnUnprocessableEntityWhenIdExistsAndAdminLoggedAndInvalidDescription() {
		putProductInstance.put("description", "Lorem");
		
		existingId = 10L;
		
		JSONObject newProduct = new JSONObject(putProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(newProduct)
				.when()
					.put("/products/{id}", existingId)
						.then()
							.statusCode(422)
							.body("status", equalTo(422))
							.body("errors.message[0]", equalTo("Descrição precisa ter no mínimo 10 caracteres"));
	}
	
	@Test
	public void updateShouldReturnUnprocessableEntityWhenIdExistsAndAdminLoggedAndPriceIsNegative() {
		putProductInstance.put("price", -200.0);
		
		existingId = 10L;
		
		JSONObject newProduct = new JSONObject(putProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(newProduct)
				.when()
					.put("/products/{id}", existingId)
						.then()
							.statusCode(422)
							.body("status", equalTo(422))
							.body("errors.message[0]", equalTo("O preço deve ser positivo"));
	}
	
	@Test
	public void updateShouldReturnUnprocessableEntityWhenIdExistsAndAdminLoggedAndPriceIsZero() {
		putProductInstance.put("price", 0.0);
		
		existingId = 10L;
		
		JSONObject newProduct = new JSONObject(putProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(newProduct)
				.when()
					.put("/products/{id}", existingId)
						.then()
							.statusCode(422)
							.body("status", equalTo(422))
							.body("errors.message[0]", equalTo("O preço deve ser positivo"));
	}
	
	@Test
	public void updateShouldReturnUnprocessableEntityWhenIdExistsAndAdminLoggedAndProductHasNoCategory() {
		putProductInstance.put("categories", null);
		
		existingId = 10L;
		
		JSONObject newProduct = new JSONObject(putProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(newProduct)
				.when()
					.put("/products/{id}", existingId)
						.then()
							.statusCode(422)
							.body("status", equalTo(422))
							.body("errors.message[0]", equalTo("Deve ter pelo menos uma categoria"));
	}
	
	@Test
	public void updateShouldReturnForbiddenWhenIdExistsAndClientLogged() {
		existingId = 10L;
		
		JSONObject newProduct = new JSONObject(putProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + clientToken)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(newProduct)
				.when()
					.put("/products/{id}", existingId)
						.then()
							.statusCode(403);
	}
	
	@Test
	public void updateShouldReturnUnauthorizedWhenIdExistsAndInvalidToken() {
		existingId = 10L;
		
		JSONObject newProduct = new JSONObject(putProductInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + invalidToken)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(newProduct)
				.when()
					.put("/products/{id}", existingId)
						.then()
							.statusCode(401);
	}
	
	@Test
	public void deleteShouldReturnNoContentWhenAdminLogged() {
		existingId = 25L;
		
		given()
			.header("Authorization", "Bearer " + adminToken)
				.when()
					.delete("/products/{id}", existingId)
						.then()
							.statusCode(204);
	}

	@Test
	public void deleteShouldReturnNotFoundWhenAdminLoggedAndNonExistingId() {
		nonExistingId = 100L;
		
		given()
			.header("Authorization", "Bearer " + adminToken)
				.when()
					.delete("/products/{id}", nonExistingId)
						.then()
							.statusCode(404)
							.body("status", equalTo(404))
							.body("error", equalTo("Recurso não encontrado"));
	}
	
	@Test
	public void deleteShouldReturnBadRequestWhenAdminLoggedAndDependentId() {
		dependentId = 3L;
		
		given()
			.header("Authorization", "Bearer " + adminToken)
				.when()
					.delete("/products/{id}", dependentId)
						.then()
							.statusCode(400)
							.body("status", equalTo(400))
							.body("error", equalTo("Falha de integridade referencial"));
	}
	
	@Test
	public void deleteShouldReturnForbiddenWhenClientLogged() {
		existingId = 24L;
		
		given()
			.header("Authorization", "Bearer " + clientToken)
				.when()
					.delete("/products/{id}", existingId)
						.then()
							.statusCode(403);
	}
	
	@Test
	public void deleteShouldReturnUnauthorizedWhenInvalidToken() {
		existingId = 24L;
		
		given()
			.header("Authorization", "Bearer " + invalidToken)
				.when()
					.delete("/products/{id}", existingId)
						.then()
							.statusCode(401);
	}

}
