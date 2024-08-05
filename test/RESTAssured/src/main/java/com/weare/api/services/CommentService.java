package com.weare.api.services;

import com.weare.api.models.Comment;
import com.weare.api.utils.JSONRequests;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class CommentService {
    private CommentService() {
    }

    public static String commentRequest(Comment comment) {
        return String.format(JSONRequests.COMMENT,
                comment.getContent(), comment.getPostId(), comment.getUserId());
    }

    public static Response createComment(String commentJsonBody, Cookie cookie) {
        return given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", cookie.getValue())
                .body(commentJsonBody)
                .when()
                .post();
    }

    public static Response showComment(Cookie cookie, Integer postId) {
        return given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", cookie.getValue())
                .queryParam("postId", postId)
                .when()
                .get();
    }

    public static Response getComment(Cookie cookie) {
        return given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", cookie)
                .when()
                .get();
    }

    public static Response editComment(Cookie cookie, Integer commentId, String editContent) {
        return given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", cookie.getValue())
                .queryParam("commentId", commentId)
                .queryParam("content", editContent)
                .when()
                .put();
    }

    public static Response likeComment(Cookie cookie, Integer commentId) {
        return given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", cookie.getValue())
                .queryParam("commentId", commentId)
                .when()
                .post();
    }

    public static Response disComment(Cookie cookie, Integer commentId) {
        return given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", cookie.getValue())
                .queryParam("commentId", commentId)
                .when()
                .post();
    }

    public static Response getAllComment(Cookie cookie, Integer postId) {
        return given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", cookie.getValue())
                .queryParam("postId", postId)
                .when()
                .get();
    }

    public static Response getOneComment(Cookie cookie, Integer postId) {
        return given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", cookie.getValue())
                .queryParam("commentId", postId)
                .when()
                .get();
    }

    public static Response deleteComment(Cookie cookie, Integer commentId) {
        return given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", cookie.getValue())
                .queryParam("commentId", commentId)
                .when()
                .delete();
    }
}
