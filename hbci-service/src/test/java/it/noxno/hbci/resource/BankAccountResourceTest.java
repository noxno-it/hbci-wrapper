package it.noxno.hbci.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class BankAccountResourceTest {

    @Test
    @TestSecurity(user = "testuser", roles = {"user", "admin"})
    public void testListAccountsEndpoint() {
        given()
          .when().get("/api/accounts")
          .then()
             .statusCode(200);
    }
}
