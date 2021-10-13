import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pojo.PostDTO;

import java.util.Arrays;

import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class FirstTest {
    private static RequestSpecification spec;

    @BeforeAll
    static void init_spec() {
        spec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri("https://jsonplaceholder.typicode.com")
                //.addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    @Test
    public void getAllElements() {
        given(spec).
                when().
                    get("posts").
                then().
                    statusCode(is(200)).
                    body(matchesJsonSchemaInClasspath("pojo_list_schema.json")).
                    body("$.size", greaterThan(0));
    }

    @Test
    public void getElementByValidId() {
        int id = 1;

        given(spec).
                when().
                    get("posts/" + id).
                then().
                    statusCode(is(200)).
                    body(matchesJsonSchemaInClasspath("pojo_schema.json")).
                    body("id", equalTo(id));
    }

    @Test
    public void getElementByInvalidId() {
        int id = 101;

        PostDTO post =
                given(spec).
                when().
                    get("posts/" + id).
                then().
                    statusCode(is(404)).
                    extract().as(PostDTO.class);

        // Is equals to empty element (all fields are null)
        assertThat(post, equalTo(new PostDTO()));
    }

    @Test
    public void getLimitedResources() {
        int limit = 20;

        given(spec).
                    param("size", limit).
                when().
                    get("posts").
                then().
                    statusCode(is(200)).
                    body("$.size", lessThanOrEqualTo(limit));
    }

    @Test
    public void filterResourcesByValidUserId() {
        int userId = 1;

        PostDTO[] posts =
                given(spec).
                        param("userId", userId).
                    when().
                        get("posts").
                    then().
                        statusCode(is(200)).
                        extract().as(PostDTO[].class);

        assertThat(posts.length, greaterThan(0));
        // All requested items contain specified userId
        assertThat(
                Arrays.stream(posts).
                        allMatch(p -> p.getUserId().equals(userId)),
                is(true));
    }

    @Test
    public void filterResourcesByInvalidUserId() {
        long userId = 100;

        PostDTO[] posts =
                given(spec).
                        param("userId", userId).
                    when().
                        get("posts").
                    then().
                        statusCode(is(200)).
                        extract().as(PostDTO[].class);

        // There are no elements in response bc userId is invalid
        assertThat(posts.length, is(0));
    }

    @Test
    public void filterByValid2Parameters() {
        int id = 5;
        int userId = 1;

        PostDTO[] posts =
                given(spec).
                        param("id", id).
                        param("userId", userId).
                    when().
                        get("posts").
                    then().
                        statusCode(is(200)).
                        extract().as(PostDTO[].class);

        assertThat(posts.length, greaterThan(0));
        assertThat(
                Arrays.stream(posts)
                        .allMatch(p -> p.getId().equals(id) &&
                                        p.getUserId() == userId),
                is(true));
    }

    @Test
    public void filterByValid4Parameters() {
        int userId = 1;
        String title = "nesciunt quas odio";
        String body = "repudiandae veniam quaerat sunt sed\nalias aut fugiat sit autem sed est\nvoluptatem omnis possimus esse voluptatibus quis\nest aut tenetur dolor neque";

        PostDTO[] posts =
                given(spec).
                        param("userId", userId).
                        param("title", title).
                        param("body", body).
                    when().
                        get("posts").
                    then().
                        statusCode(200).
                        extract().as(PostDTO[].class);

        assertThat(posts.length, greaterThan(0));
        assertThat(
                Arrays.stream(posts)
                        .allMatch(p -> p.getUserId() == userId &&
                                        p.getTitle().equals(title) &&
                                        p.getBody().equals(body)),
                is(true));
    }
}
