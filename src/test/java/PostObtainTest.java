import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
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

public class PostObtainTest {
    private static RequestSpecification spec;
    private static ResponseSpecification commonResponseSpec;

    @BeforeAll
    static void init_spec() {
        spec = new RequestSpecBuilder().
                setContentType(ContentType.JSON).
                setBaseUri("https://jsonplaceholder.typicode.com").
                addFilter(new RequestLoggingFilter()).
                addFilter(new ResponseLoggingFilter()).
                build();

        // commonResponseSpec is specification used in every response
        commonResponseSpec = new ResponseSpecBuilder().
                expectContentType("application/json; charset=utf-8").
                build();
    }

    /**
     * The test queries for all posts
     * Expected: 200 OK
     *           response body matches for specified JSON schema
     */
    @Test
    public void getAllElements() {
        given(spec).
                when().
                    get("posts").
                then().
                    statusCode(is(HttpStatus.SC_OK)).
                    spec(commonResponseSpec).
                    body(matchesJsonSchemaInClasspath("schema/post_list_schema.json")).
                    body("$.size", greaterThan(0));
    }

    /**
     * The test queries for post with valid id
     * Expected: 200 OK
     *           post with queried id
     */
    @ParameterizedTest
    @ValueSource(ints = {1, 50, 100})
    public void getElementByValidId(int id) {
        given(spec).
                when().
                    get("posts/" + id).
                then().
                    statusCode(is(HttpStatus.SC_OK)).
                    spec(commonResponseSpec).
                    body(matchesJsonSchemaInClasspath("schema/post_schema.json")).
                    body("id", equalTo(id));
    }

    /**
     * The test queries for post with invalid id
     * Expected: 404 NOT_FOUND
     *           empty response body
     */
    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 101, Integer.MAX_VALUE, Integer.MIN_VALUE})
    public void getElementByInvalidId(int id) {
        given(spec).
            when().
                get("posts/" + id).
            then().
                statusCode(is(HttpStatus.SC_NOT_FOUND)).
                spec(commonResponseSpec).
                body("isEmpty()", is(true));
    }

    /**
     * The test queries for posts filtered by valid id parameter
     * Expected: 200 OK
     *           posts with queried id
     *           response body matches for specified JSON schema
     */
    @ParameterizedTest
    @ValueSource(ints = {1, 100, 50})
    public void filterResourcesByValidId(int id) {

        PostDTO[] posts =
                given(spec).
                        param("id", id).
                    when().
                        get("posts").
                    then().
                        statusCode(is(HttpStatus.SC_OK)).
                        spec(commonResponseSpec).
                        extract().as(PostDTO[].class);

        assertThat(posts.length, greaterThan(0));
        assertThat(
                Arrays.stream(posts).
                        allMatch(p -> p.getId().equals(id)),
                is(true));
    }

    /**
     * The test queries for post with invalid id parameter
     * Expected: 200 OK
     *           empty JSON array
     */
    @ParameterizedTest
    @ValueSource(ints = {-1, 101, Integer.MAX_VALUE, Integer.MIN_VALUE})
    public void filterResourcesByInvalidId(int id) {
        given(spec).
                param("id", id).
            when().
                get("posts").
            then().
                statusCode(is(HttpStatus.SC_OK)).
                spec(commonResponseSpec).
                body("$.size", is(0));
    }

    /**
     * The test queries for posts with existent parameters
     * Expected: 200 OK
     *           posts parameters are equals to queried parameters
     *           response body matches for specified JSON schema
     */
    @ParameterizedTest
    @CsvFileSource(resources = {"filter/filter_valid_posts.csv"})
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
                        spec(commonResponseSpec).
                        body(matchesJsonSchemaInClasspath("schema/post_list_schema.json")).
                        extract().as(PostDTO[].class);

        assertThat(posts.length, greaterThan(0));
        assertThat(
                Arrays.stream(posts)
                        .allMatch(p -> p.getUserId() == userId &&
                                        p.getTitle().equals(title) &&
                                        p.getBody().equals(body)),
                is(true));
    }

    /**
     * The test queries for posts with nonexistent parameters
     * Expected: 200 OK
     *           empty JSON array
     */
    @ParameterizedTest
    @CsvFileSource(resources = {"filter/filter_invalid_posts.csv"})
    public void filterByInvalidParameters(int userId, String title, String body) {
        given(spec).
                param("userId", userId).
                param("title", title).
                param("body", body).
            when().
                get("posts").
            then().
                statusCode(is(HttpStatus.SC_OK)).
                spec(commonResponseSpec).
                body("$.size", is(0));
    }
}
