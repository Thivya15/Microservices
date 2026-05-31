package com.microservices.order_service;

import com.microservices.order_service.stubs.InventoryClientStub;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.testcontainers.containers.MySQLContainer;

import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class OrderServiceApplicationTests {

	@ServiceConnection
	static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.3.0");

	@LocalServerPort
	private Integer port;

	@BeforeEach
	void setUp() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}

	@Test
	void shouldSubmitOrder_WhenProductIsInStock() {

		String requestBody = """
                {
                  "skuCode": "iphone_15",
                  "price": 1000,
                  "quantity": 1
                }
                """;

		InventoryClientStub.stubInventoryCall("iphone_15", 1); // ✅ Stub setup

		var response = RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post("/api/order")
				.then()
				.log().all()
				.statusCode(201) // ✅ Order placed
				.extract()
				.body()
				.asString();

		assertThat(response, Matchers.equalTo("Order Placed Successfully"));
	}

//	@Test
//	void shouldNotSubmitOrder_WhenProductIsOutOfStock() {
//
//		// ✅ Out of stock → false
//		wireMockServer.stubFor(
//				get(urlPathEqualTo("/api/inventory"))
//						.withQueryParam("skuCode", equalTo("iphone_15"))
//						.withQueryParam("quantity", equalTo("1"))
//						.willReturn(aResponse()
//								.withStatus(200)
//								.withHeader("Content-Type", "application/json")
//								.withBody("false"))
//		);
//
//		String requestBody = """
//                {
//                  "skuCode": "iphone_15",
//                  "price": 1000,
//                  "quantity": 1
//                }
//                """;
//
//		RestAssured.given()
//				.contentType("application/json")
//				.body(requestBody)
//				.when()
//				.post("/api/order")
//				.then()
//				.log().all()
//				.statusCode(500); // ✅ Out of stock → RuntimeException → 500
//	}
}