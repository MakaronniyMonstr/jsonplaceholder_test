import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import dto.PostDTO;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PostControllerTest {
    private static RequestSpecification spec;

    @BeforeAll
    static void init_spec() {
        spec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri("https://jsonplaceholder.typicode.com")
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    @Test
    public void getAllElements() {
        given(spec).
                when().
                    get("posts").
                then().
                    statusCode(is(HttpStatus.SC_OK)).
                    body(matchesJsonSchemaInClasspath("post_list_schema.json")).
                    body("$.size", greaterThan(0));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 50, 100})
    public void getElementByValidId(int id) {
        given(spec).
                when().
                    get("posts/" + id).
                then().
                    statusCode(is(HttpStatus.SC_OK)).
                    body(matchesJsonSchemaInClasspath("post_schema.json")).
                    body("id", equalTo(id));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 101, Integer.MAX_VALUE, Integer.MIN_VALUE})
    public void getElementByInvalidId(int id) {
        given(spec).
            when().
                get("posts/" + id).
            then().
                statusCode(is(HttpStatus.SC_NOT_FOUND)).
                body("isEmpty()", is(true)).
                extract().as(PostDTO.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 100, 50})
    public void filterResourcesByValidId() {
        int userId = 1;

        PostDTO[] posts =
                given(spec).
                        param("userId", userId).
                    when().
                        get("posts").
                    then().
                        statusCode(is(HttpStatus.SC_OK)).
                        extract().as(PostDTO[].class);

        assertThat(posts.length, greaterThan(0));
        // All requested items contain specified userId
        assertThat(
                Arrays.stream(posts).
                        allMatch(p -> p.getUserId().equals(userId)),
                is(true));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 101, Integer.MAX_VALUE, Integer.MIN_VALUE})
    public void filterResourcesByInvalidId(int id) {
        given(spec).
                param("id", id).
            when().
                get("posts").
            then().
                statusCode(is(HttpStatus.SC_OK)).
                body("$.size", is(0));
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
                        statusCode(is(HttpStatus.SC_OK)).
                        extract().as(PostDTO[].class);

        assertThat(posts.length, greaterThan(0));
        assertThat(
                Arrays.stream(posts)
                        .allMatch(p -> p.getId().equals(id) &&
                                        p.getUserId() == userId),
                is(true));
    }

    @ParameterizedTest
    @CsvFileSource(resources = {"filter_valid_posts.csv"})
    public void filterByValidParameters(int userId, String title, String body) {
        PostDTO[] posts =
                given(spec).
                        param("userId", userId).
                        param("title", title).
                        param("body", body).
                    when().
                        get("posts").
                    then().
                        statusCode(is(HttpStatus.SC_OK)).
                        body(matchesJsonSchemaInClasspath("post_list_schema.json")).
                        extract().as(PostDTO[].class);

        assertThat(posts.length, greaterThan(0));
        assertThat(
                Arrays.stream(posts)
                        .allMatch(p -> p.getUserId() == userId &&
                                        p.getTitle().equals(title) &&
                                        p.getBody().equals(body)),
                is(true));
    }

    /*
    Test queries resource with
     */
    @ParameterizedTest
    @CsvFileSource(resources = {"filter_invalid_posts.csv"})
    public void filterByInvalidParameters(int userId, String title, String body) {
        given(spec).
                param("userId", userId).
                param("title", title).
                param("body", body).
            when().
                get("posts").
            then().
                statusCode(is(HttpStatus.SC_OK)).
                body("$.size", is(0));
    }
}
