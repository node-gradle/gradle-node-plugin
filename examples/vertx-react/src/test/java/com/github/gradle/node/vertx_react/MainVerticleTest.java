package com.github.gradle.node.vertx_react;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import static io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class MainVerticleTest {
    private FirefoxDriver firefoxDriver;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
        firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions()
                .setHeadless(true);
        firefoxDriver = new FirefoxDriver(options);
    }

    @Test
    void shouldServeTheReactApplication(Vertx vertx, VertxTestContext testContext) throws Exception {
        String url = "http://localhost:8888/";
        firefoxDriver.get(url);
        assertThat(firefoxDriver.findElement(By.cssSelector("header p")).getText())
                .isEqualTo("Edit src/App.js and save to reload.");
        testContext.completeNow();
    }

    @AfterEach
    void tearDown() {
        firefoxDriver.quit();
    }
}
