package base;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.weare.api.models.User;
import com.weare.api.services.UserService;
import com.weare.api.utils.AssertHelper;
import com.weare.api.utils.DatabaseOperations;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import org.junit.jupiter.api.*;

import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static com.weare.api.utils.Endpoints.*;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.apache.http.HttpStatus.*;

public class BaseTestSetup {
    protected String username;
    protected String password;
    protected Integer userId;
    protected User user;
    protected Cookie cookie;

    protected void beforeEach() {
        register(user);
    }

    protected void register(User user) {
        baseURI = String.format("%s%s", BASE_URL, REGISTER_ENDPOINT);

        String registrationJsonBody = UserService.registrationRequest(user);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(registrationJsonBody)
                .when()
                .post();

        String responseBody = response.getBody().asString();

        int statusCode = response.getStatusCode();
        AssertHelper.assertStatusCode(statusCode, SC_OK);
        AssertHelper.assertNotEmptyResponse(responseBody);

        String regex = "name (\\w+)(.*)id (\\d+)";
        Matcher matcher = Pattern.compile(regex).matcher(responseBody);
        if (matcher.find()) {
            username = matcher.group(1);
            userId = Integer.parseInt(matcher.group(3));
        }

        user.setUserId(userId);

        AssertHelper.assertUsernameEquals(username, user.getUsername());
        AssertHelper.assertPositiveUserId(userId);
        System.out.println(response.asString());

        boolean userExists = DatabaseOperations.checkUserExists(userId);
        AssertHelper.assertUserExists(userExists, userId);
    }

    public void login(User user) {
        baseURI = format("%s%s", BASE_URL, AUTH_ENDPOINT);

        Response response = getApplicationAuthentication(user.getUsername(), user.getPassword())
                .when()
                .post();

        if (response.getDetailedCookie("JSESSIONID") != null) {
            cookie = response.getDetailedCookie("JSESSIONID");
        } else {
            cookie = createAuthenticationCookieWithValue("value");
        }

        int statusCode = response.getStatusCode();
        boolean isValidStatusCode = (statusCode == SC_OK) || (statusCode == SC_MOVED_TEMPORARILY);
        Assertions.assertTrue(isValidStatusCode, "Incorrect status code. Expected Status 200.");
        System.out.println("User authenticated successfully - Username: " + user.getUsername() + " - Cookie: " + cookie.getValue());
    }

    protected void logout() {
        cookie = null;
    }

    protected Cookie createAuthenticationCookieWithValue(String value) {
        return new Cookie.Builder("JSESSIONID", value).setPath("/").build();
    }

    @BeforeAll
    public static void setup() {
        EncoderConfig encoderConfig = RestAssured.config().getEncoderConfig()
            .appendDefaultContentCharsetToContentTypeIfUndefined(false);

        RestAssured.config = RestAssured.config().encoderConfig(encoderConfig);
    }

    public RequestSpecification getApplicationAuthentication(String username, String password) {
        return given()
                .multiPart("username", username)
                .multiPart("password", password);
    }
}
