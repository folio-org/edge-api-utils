package org.folio.edge.api.utils;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class MappersTest {

  @Test
  public void testJsonMapper() throws Exception {
    String key = "foo";
    String value = "bar";

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode();
    node.put(key, value);

    String json = node.toPrettyString();
    Map<?, ?> asObj = Mappers.jsonMapper.readValue(json, HashMap.class);

    assertEquals(value, asObj.get(key));
    assertEquals(json, Mappers.jsonMapper.writeValueAsString(asObj));
  }

  @Test
  public void testXmlMapper() throws Exception {
    String a = "foo";
    String b = "bar";

    StringBuilder sb = new StringBuilder();
    sb.append(Mappers.XML_PROLOG)
      .append("<test>")
      .append(System.lineSeparator())
      .append("  <a>").append(a).append("</a>")
      .append(System.lineSeparator())
      .append("  <b>").append(b).append("</b>")
      .append(System.lineSeparator())
      .append("</test>")
      .append(System.lineSeparator());

    TestObject obj = new TestObject(a, b);

    String asStr = Mappers.xmlMapper.writeValueAsString(obj);
    assertEquals(sb.toString(), Mappers.XML_PROLOG + asStr);

    TestObject asObj = Mappers.xmlMapper.readValue(asStr, TestObject.class);
    assertEquals(obj.a, asObj.a);
    assertEquals(obj.b, asObj.b);
  }

  @JacksonXmlRootElement(localName = "test")
  public static class TestObject {

    @JacksonXmlProperty(localName = "a")
    private String a;
    @JacksonXmlProperty(localName = "b")
    private String b;

    public TestObject(@JacksonXmlProperty(localName = "a") String a, @JacksonXmlProperty(localName = "b") String b) {
      this.a = a;
      this.b = b;
    }
  }

}
