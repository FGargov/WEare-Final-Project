package weare.api.tests;

import base.BaseTestSetup;
import com.weare.api.models.Post;
import com.weare.api.models.Skill;
import com.weare.api.models.User;
import com.weare.api.services.PostService;
import com.weare.api.services.UserService;
import com.weare.api.utils.AssertHelper;
import com.weare.api.utils.Constants;
import com.weare.api.utils.JSONRequests;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static com.weare.api.services.UserService.*;
import static com.weare.api.utils.Constants.POST_ID;
import static com.weare.api.utils.Endpoints.*;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.apache.http.HttpStatus.SC_OK;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class UserTests extends BaseTestSetup {
    private User user;
    Post post;
    Skill skill;

    @Override @BeforeEach
    protected void beforeEach() {
        user = new User();
        register(user);
        login(user);
    }

    @AfterEach
    public void tearDownAfterTest() {
        logout();
    }
    @Feature("User Profile")
    @Story("Update personal profile")
    @Description("Test to verify that a user can successfully update their personal profile.")
    @Test
    public void updatePersonalProfileTest() {
        String formattedEndpoint = String.format(UPDATE_PERSONAL_PROFILE_ENDPOINT, userId);
        baseURI = format("%s%s", BASE_URL,formattedEndpoint);

        String updateUserBody = UserService.updateProfileRequest(user);

        Response response = given()
                .cookie(cookie.getName(), cookie.getValue())
                .contentType(ContentType.JSON)
                .body(updateUserBody)
                .when()
                .post();

        int statusCode = response.getStatusCode();
        AssertHelper.assertStatusCode(statusCode, SC_OK);
    }

    @Feature("User Search")
    @Story("Retrieve user by ID")
    @Description("Test to verify that a user can be retrieved by their unique ID.")
    @Test
    public void getUserByIdTest() {
        String formattedEndpoint = String.format(USER_BY_ID_ENDPOINT, userId);
        baseURI = format("%s%s", BASE_URL, formattedEndpoint);

        Response response = getUserById(cookie, user.getUsername());

        int statusCode = response.getStatusCode();
        String resUserId = response.getBody().jsonPath().getString("id");
        String resUsername = response.getBody().jsonPath().getString("username");
        String resEmail = response.getBody().jsonPath().getString("email");

        AssertHelper.assertStatusCode(statusCode, SC_OK);
        AssertHelper.assertResponseBodyNotNull(response.getBody());
        AssertHelper.assertUserIdEquals(Integer.parseInt(resUserId), user.getUserId());
        AssertHelper.assertUsernameEquals(resUsername, user.getUsername());
        AssertHelper.assertEmailEquals(resEmail, user.getEmail());
    }

    @Feature("User Search")
    @Story("Search user by username")
    @Description("Test to verify that users can be searched by their username.")
    @Test
    public void searchByUserTest() {
        baseURI = format("%s%s", BASE_URL, SEARCH_USER_ENDPOINT);

        String searchUserBody = UserService.searchUserRequest(user);
        Response response = searchByUser(searchUserBody, cookie);

        int statusCode = response.getStatusCode();
        AssertHelper.assertStatusCode(statusCode, SC_OK);

        String resUserId = response.getBody().jsonPath().getString("[0].userId");
        String resUsername = response.getBody().jsonPath().getString("[0].username");

        AssertHelper.assertUserIdEquals(Integer.parseInt(resUserId), user.getUserId());
        AssertHelper.assertUsernameEquals(resUsername, user.getUsername());
    }

    @Feature("Posts")
    @Story("Create new post")
    @Description("Test to verify that a user can create a new post.")
    @Test
    public void createPostTest() {
        baseURI = format("%s%s", BASE_URL, CREATE_POST_ENDPOINT);
        post = new Post();
        post.setPublic(true);

        String postJsonBody = PostService.postRequest(post);
        Response response = createPost(postJsonBody, cookie);

        POST_ID = response.getBody().jsonPath().get("postId");
        int statusCode = response.getStatusCode();
        String contentPost = response.getBody().jsonPath().get("content");
        Boolean privatePost = response.getBody().jsonPath().get("public");

        AssertHelper.assertStatusCode(statusCode, SC_OK);
        AssertHelper.assertPostIsPrivate(privatePost);
        AssertHelper.assertContentTypeNotNull(ContentType.JSON);
        AssertHelper.assertContentEquals(contentPost, post.getContent());
    }

    @Feature("User Search")
    @Story("Search user's posts")
    @Description("Test to verify that a user's posts can be searched and retrieved.")
    @Test
    public void searchUserPostsTest() {
        createPostTest();
        String formattedEndpoint = format(SEARCH_USER_POSTS_ENDPOINT, userId) ;
        baseURI = format("%s%s", BASE_URL, formattedEndpoint);

        Response response = searchUserPosts(JSONRequests.SHOW_PROFILE_POSTS, cookie);

        int statusCode = response.getStatusCode();
        AssertHelper.assertStatusCode(statusCode, SC_OK);

        String resPostId = response.getBody().jsonPath().getString("[0].postId");
        Assertions.assertEquals(Integer.parseInt(resPostId), POST_ID, "Incorrect user's post ID");
        String postContent = response.getBody().jsonPath().getString("[0].content");
        Assertions.assertEquals(postContent, post.getContent(), "The content of the post is not the same.");
    }

    @Feature("User Profile")
    @Story("Update user expertise")
    @Description("Test to verify that a user can update their expertise profile.")
    @Test
    public void updateUserExpertiseTest() {
        String formattedString = format(UPDATE_USER_EXPERTISE_ENDPOINT, userId);
        baseURI = format("%s%s", BASE_URL, formattedString);

        skill = new Skill();
        String updateUserExpertiseBody = UserService.updateExpertiseProfileRequest(user, skill);
        Response response = updateUserExpertise(updateUserExpertiseBody, cookie);

        int statusCode = response.getStatusCode();
        AssertHelper.assertStatusCode(statusCode, SC_OK);

        Integer resUserId = response.getBody().jsonPath().get("id");
        Integer resCategoryId = response.getBody().jsonPath().get("category.id");
        Float availability = response.getBody().jsonPath().get("availability");
        AssertHelper.assertUserIdEquals(resUserId, user.getUserId());
        AssertHelper.assertCategoryIdNotNull(resCategoryId);
        AssertHelper.assertCategoryIdsMatch(resCategoryId, user.getCategoryId());
        AssertHelper.assertAvailabilityMatches(availability, Float.parseFloat(Constants.AVAILABILITY));
    }
}

