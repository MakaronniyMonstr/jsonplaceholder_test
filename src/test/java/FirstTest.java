import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pojo.PojoModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class FirstTest {
    private static RequestSpecification spec;
    private static Gson gson;
    private PojoModel[] overallElements;

    @BeforeAll
    static void init_spec() {
        spec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri("https://jsonplaceholder.typicode.com")
                .build();

        gson = new GsonBuilder().create();
    }

    @Test
    public void get_overall_elements() {
        Response response = given(spec)
                .when()
                .get("posts");

        assertThat(response.statusCode(), is(200));
        overallElements = gson.fromJson(response.body().asString(), PojoModel[].class);
        assertThat(overallElements.length, greaterThan(0));
    }

    @Test
    public void get_limited_resources() {
        Response response = given(spec)
                .param("limit", 1)
                .when()
                .get("posts");

        assertThat(response.statusCode(), is(200));
        PojoModel[] pojoModels = gson.fromJson(response.body().asString(), PojoModel[].class);
        assertThat(pojoModels.length, lessThanOrEqualTo(20));
    }

    @Test
    public void get_resource_by_valid_user_id() {
        Response response = given(spec)
                .param("userId", 1)
                .when()
                .get("posts");

        assertThat(response.getStatusCode(), is(200));
        List<PojoModel> pojoModels = Arrays.asList(gson.fromJson(response.body().asString(), PojoModel[].class));
        System.out.println(pojoModels.get(0).getUserId());
        assertThat(pojoModels.stream().allMatch(m -> m.getUserId().equals(1L)), is(true));
    }

    @Test
    public void get_resource_by_invalid_user_id() {
        Response response = given(spec)
                .param("userId", 100)
                .when()
                .get("posts");

        assertThat(response.getStatusCode(), is(200));

        List<PojoModel> pojoModels = gson.fromJson(response.body().asString(), ArrayList.class);
        assertThat(pojoModels.size(), is(0));
    }
}
