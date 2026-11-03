package com.hotel.desktop.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceLoadTest {

    @ParameterizedTest(name = "FXML resource {0} exists and is well-formed XML")
    @ValueSource(strings = {
            "/fxml/main-view.fxml",
            "/fxml/login.fxml",
            "/fxml/register.fxml",
            "/fxml/home-view.fxml",
            "/fxml/main-layout.fxml",
            "/fxml/placeholder-page.fxml"
    })
    @DisplayName("Verify all FXML view files exist and parse as valid XML")
    void testFxmlResourcesExistAndAreValidXml(String fxmlPath) throws Exception {
        URL fxmlUrl = getClass().getResource(fxmlPath);
        assertNotNull(fxmlUrl, "FXML resource " + fxmlPath + " must be present on the classpath");

        try (InputStream is = fxmlUrl.openStream()) {
            assertNotNull(is);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(is);
            assertNotNull(doc);
            assertNotNull(doc.getDocumentElement(), "FXML must have a root element");
        }
    }

    @Test
    @DisplayName("Verify CSS stylesheet exists and is non-empty")
    void testCssResourceExistsAndIsNonEmpty() throws Exception {
        URL cssUrl = getClass().getResource("/css/application.css");
        assertNotNull(cssUrl, "CSS resource /css/application.css must be present on the classpath");

        try (InputStream is = cssUrl.openStream()) {
            assertNotNull(is);
            byte[] bytes = is.readAllBytes();
            assertTrue(bytes.length > 500, "CSS stylesheet must contain style definitions");
        }
    }
}
