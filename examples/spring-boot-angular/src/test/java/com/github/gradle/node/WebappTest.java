package com.github.gradle.node;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SpringBootAngularApplication.class, webEnvironment = RANDOM_PORT)
class WebappTest {
    @LocalServerPort
    protected int port;
    private FirefoxDriver firefoxDriver;

    @BeforeEach
    public void setUp() {
        firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions()
                .setHeadless(true);
        firefoxDriver = new FirefoxDriver(options);
    }

    @Test
    void shouldServeWebapp() {
        String url = "http://localhost:" + port + "/";
        firefoxDriver.get(url);
        assertEquals("webapp app is running!",
                firefoxDriver.findElement(By.cssSelector(".card.highlight-card span")).getText());
    }

    @AfterEach
    void tearDown() {
        firefoxDriver.quit();
    }
}
