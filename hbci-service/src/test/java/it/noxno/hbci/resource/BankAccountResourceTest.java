package it.noxno.hbci.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class BankAccountResourceTest {

    @Test
    public void testListAccountsEndpoint() {
        given()
          .when().get("/api/accounts")
          .then()
             .statusCode(200);
    }
}
