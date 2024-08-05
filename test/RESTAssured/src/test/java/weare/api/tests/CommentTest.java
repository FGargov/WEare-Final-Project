package weare.api.tests;

import base.BaseTestSetup;
import com.weare.api.models.Comment;
import com.weare.api.models.Post;
import com.weare.api.models.User;
import com.weare.api.services.CommentService;
import com.weare.api.services.PostService;
import com.weare.api.services.UserService;
import com.weare.api.utils.AssertHelper;
import com.weare.api.utils.DatabaseOperations;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.weare.api.utils.Constants.COMMENT_ID;
import static com.weare.api.utils.Constants.POST_ID;
import static com.weare.api.utils.Endpoints.*;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.apache.http.HttpStatus.SC_OK;

public class CommentTest extends BaseTestSetup {
    protected static Post post;
    protected static Comment comment;
    protected Integer userId;

    @Override
    @BeforeEach
    protected void beforeEach() {
        user = new User();
        register(user);
        login(user);
    }

    @AfterEach
    public void tearDownAfterTest() {
        if (user != null) {
            DatabaseOperations.deleteLikesForUserComments(user.getUserId());
            DatabaseOperations.deleteCommentsForUserPosts(user.getUserId());
            DatabaseOperations.deleteLikesForUserPosts(user.getUserId());
            DatabaseOperations.deletePostsByUserId(user.getUserId());
        }
        logout();
        DatabaseOperations.removeUserById("user_id", user.getUserId());
    }


    @Feature("Comments")
    @Story("Create a comment")
    @Description("Test to verify that a comment can be added to a post successfully.")
    @Test
    public void createComment() {
        baseURI = format("%s%s", BASE_URL, CREATE_POST_ENDPOINT);
        post = new Post();
        post.setPublic(true);

        String postJsonBody = PostService.postRequest(post);
        Response response = PostService.createPublicPost(postJsonBody, cookie);
        POST_ID = response.getBody().jsonPath().get("postId");

        baseURI = format("%s%s", BASE_URL, CREATE_COMMENT_ENDPOINT);
        comment = new Comment();
        userId = user.getUserId();
        comment.setUserId(userId);
        comment.setPostId(POST_ID);


        String commentJsonBody = CommentService.commentRequest(comment);
        Response commentResponse = CommentService.createComment(commentJsonBody, cookie);
        COMMENT_ID = response.getBody().jsonPath().get("commentId");

        int statusCode = commentResponse.getStatusCode();
        String contentComment = commentResponse.getBody().jsonPath().get("content");
        String responseBody = commentResponse.getBody().asString();
        System.out.print(responseBody);

        AssertHelper.assertStatusCode(SC_OK, statusCode);
        AssertHelper.assertContentTypeNotNull(ContentType.JSON);
        AssertHelper.assertContentEquals(contentComment, comment.getContent());

        CommentService.deleteComment(cookie, commentResponse.getBody().jsonPath().get("commentId"));
        PostService.deletePost(cookie, POST_ID);
    }

    @Feature("Comments")
    @Story("Retrieve a comment")
    @Description("Test to verify that a comment can be retrieved successfully.")
    @Test
    public void getComment() {
        baseURI = format("%s%s", BASE_URL, GET_COMMENT);

        Response response = CommentService.getComment(cookie);
        String responseBody = response.getBody().asString();
        System.out.print(responseBody);

        int statusCode = response.getStatusCode();
        AssertHelper.assertStatusCode(statusCode, SC_OK);
    }


    @Feature("Comments")
    @Story("Edit a comment")
    @Description("Test to verify that a comment can be edited successfully.")
    @Test
    public void editComment() {
        baseURI = format("%s%s", BASE_URL, CREATE_POST_ENDPOINT);
        post = new Post();
        post.setPublic(true);

        String postJsonBody = PostService.postRequest(post);
        Response response = PostService.createPublicPost(postJsonBody, cookie);

        POST_ID = response.getBody().jsonPath().get("postId");

        baseURI = format("%s%s", BASE_URL, CREATE_COMMENT_ENDPOINT);
        comment = new Comment();

        userId = user.getUserId();
        comment.setUserId(userId);
        comment.setPostId(POST_ID);

        String commentJsonBody = CommentService.commentRequest(comment);
        response = CommentService.createComment(commentJsonBody, cookie);
        COMMENT_ID = response.getBody().jsonPath().get("commentId");

        baseURI = format("%s%s", BASE_URL, EDIT_COMMENT);

        response = CommentService.editComment(cookie, COMMENT_ID, comment.getContent() + " edited comment");

        int statusCode = response.getStatusCode();
        AssertHelper.assertStatusCode(statusCode, SC_OK);
    }

    @Feature("Comments")
    @Story("Like a comment")
    @Description("Test to verify that a comment can be liked successfully.")
    @Test
    public void likeComment() {
        baseURI = format("%s%s", BASE_URL, CREATE_POST_ENDPOINT);
        post = new Post();
        post.setPublic(true);

        String postJsonBody = PostService.postRequest(post);
        Response response = UserService.createPost(postJsonBody, cookie);

        POST_ID = response.getBody().jsonPath().get("postId");

        baseURI = format("%s%s", BASE_URL, CREATE_COMMENT_ENDPOINT);

        comment = new Comment();
        userId = user.getUserId();
        comment.setUserId(userId);
        comment.setPostId(POST_ID);

        String commentJsonBody = CommentService.commentRequest(comment);
        response = CommentService.createComment(commentJsonBody, cookie);
        COMMENT_ID = response.getBody().jsonPath().get("commentId");
        baseURI = format("%s%s", BASE_URL, LIKE_COMMENT);
        comment.setLike(true);

        Response commentResponse = CommentService.likeComment(cookie, COMMENT_ID);
        String responseBody = response.getBody().asString();
        System.out.print(responseBody);

        int statusCode = response.getStatusCode();
        AssertHelper.assertStatusCode(statusCode, SC_OK);
        CommentService.deleteComment(cookie, commentResponse.getBody().jsonPath().get("commentId"));
        PostService.deletePost(cookie, POST_ID);
    }

    @Feature("Comments")
    @Story("Dislike a comment")
    @Description("Test to verify that a comment can be disliked successfully.")
    @Test
    public void dislikeComment() {
        baseURI = format("%s%s", BASE_URL, CREATE_POST_ENDPOINT);
        post = new Post();
        post.setPublic(true);

        String postJsonBody = PostService.postRequest(post);
        Response response = PostService.createPublicPost(postJsonBody, cookie);

        POST_ID = response.getBody().jsonPath().get("postId");

        baseURI = format("%s%s", BASE_URL, CREATE_COMMENT_ENDPOINT);
        comment = new Comment();
        userId = user.getUserId();
        comment.setUserId(userId);
        comment.setPostId(POST_ID);

        String commentJsonBody = CommentService.commentRequest(comment);
        response = CommentService.createComment(commentJsonBody, cookie);
        COMMENT_ID = response.getBody().jsonPath().get("commentId");

        baseURI = format("%s%s", BASE_URL, LIKE_COMMENT);
        comment.setLike(true);

        Response commentResponse = CommentService.likeComment(cookie, COMMENT_ID);
        comment.setLike(false);
        response = CommentService.disComment(cookie, COMMENT_ID);

        int statusCode = response.getStatusCode();
        AssertHelper.assertStatusCode(statusCode, SC_OK);
        CommentService.deleteComment(cookie, response.getBody().jsonPath().get("commentId"));
        PostService.deletePost(cookie, POST_ID);
    }

    @Feature("Comments")
    @Story("Retrieve all comments of a post")
    @Description("Test to verify that all comments of a post can be retrieved successfully.")
    @Test
    public void getAllComment() {
        baseURI = format("%s%s", BASE_URL, CREATE_POST_ENDPOINT);
        post = new Post();
        post.setPublic(true);

        String postJsonBody = PostService.postRequest(post);
        Response response = UserService.createPost(postJsonBody, cookie);

        POST_ID = response.getBody().jsonPath().get("postId");
        baseURI = format("%s%s", BASE_URL, FIND_ALL_COMMENTS);

        Response commentResponse = CommentService.getAllComment(cookie, POST_ID);

        int statusCode = response.getStatusCode();
        AssertHelper.assertStatusCode(statusCode, SC_OK);
    }

    @Feature("Comments")
    @Story("Retrieve a single comment")
    @Description("Test to verify that a single comment can be retrieved by its unique ID.")
    @Test
    public void getOneComment() {
        baseURI = format("%s%s", BASE_URL, CREATE_POST_ENDPOINT);
        post = new Post();
        post.setPublic(true);

        String postJsonBody = PostService.postRequest(post);
        Response response = UserService.createPost(postJsonBody, cookie);

        POST_ID = response.getBody().jsonPath().get("postId");

        baseURI = format("%s%s", BASE_URL, CREATE_COMMENT_ENDPOINT);
        comment = new Comment();
        userId = user.getUserId();
        comment.setUserId(userId);
        comment.setPostId(POST_ID);


        String commentJsonBody = CommentService.commentRequest(comment);
        response = CommentService.createComment(commentJsonBody, cookie);
        COMMENT_ID = response.getBody().jsonPath().get("commentId");
        baseURI = format("%s%s", BASE_URL, FIND_ONE_COMMENTS);

        Response commentResponse = CommentService.getOneComment(cookie, COMMENT_ID);

        int statusCode = response.getStatusCode();
        AssertHelper.assertStatusCode(statusCode, SC_OK);
        CommentService.deleteComment(cookie, commentResponse.getBody().jsonPath().get("commentId"));
        PostService.deletePost(cookie, POST_ID);
    }

    @Feature("Comments")
    @Story("Delete a comment")
    @Description("Test to verify that a comment can be deleted successfully.")
    @Test
    public void deleteComment() {
        baseURI = format("%s%s", BASE_URL, CREATE_POST_ENDPOINT);
        post = new Post();
        post.setPublic(true);

        String postJsonBody = PostService.postRequest(post);
        Response response = UserService.createPost(postJsonBody, cookie);

        POST_ID = response.getBody().jsonPath().get("postId");

        baseURI = format("%s%s", BASE_URL, CREATE_COMMENT_ENDPOINT);
        comment = new Comment();
        userId = user.getUserId();
        comment.setUserId(userId);
        comment.setPostId(POST_ID);


        String commentJsonBody = CommentService.commentRequest(comment);
        Response commentResponse = CommentService.createComment(commentJsonBody, cookie);
        COMMENT_ID = response.getBody().jsonPath().get("commentId");
        CommentService.deleteComment(cookie, commentResponse.getBody().jsonPath().get("commentId"));
        PostService.deletePost(cookie, POST_ID);

        int statusCode = response.getStatusCode();
        String responseBody = response.getBody().asString();
        System.out.println(responseBody);
        AssertHelper.assertStatusCode(statusCode, SC_OK);
    }

    @Feature("Posts")
    @Story("Delete a post")
    @Description("Test to verify that a post can be deleted successfully after all comment operations.")
    @Test
    public void deletePost() {
        baseURI = format("%s%s", BASE_URL, CREATE_POST_ENDPOINT);
        post = new Post();
        post.setPublic(true);

        String postJsonBody = PostService.postRequest(post);
        Response response = UserService.createPost(postJsonBody, cookie);

        POST_ID = response.getBody().jsonPath().get("postId");
        baseURI = format("%s%s", BASE_URL, DELETE_POST);

        response = PostService.deletePost(cookie, POST_ID);

        int statusCode = response.getStatusCode();
        String responseBody = response.getBody().asString();
        System.out.println(responseBody);
        AssertHelper.assertStatusCode(statusCode, SC_OK);
    }
}
