package com.devsuperior.dscommerce.controllers;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CategoryControllerRA {
	
	@BeforeEach
	public void setup() {
		baseURI = "http://localhost:8080";
	}
	
	@Test
	public void findAllShouldReturnListOfCategories() {
		given()
			.get("/categories")
		.then()
			.statusCode(200)
			.body("id", hasItems(1, 2, 3))
			.body("name", hasItems("Livros", "Eletrônicos", "Computadores"));
	}

}
