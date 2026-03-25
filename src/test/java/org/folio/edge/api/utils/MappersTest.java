package org.folio.edge.api.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import tools.jackson.databind.json.JsonMapper;

public class MappersTest {

  @Test
  public void testJsonMapper() throws Exception {
    String key = "foo";
    String value = "bar";

    var mapper = JsonMapper.shared();
    var node = mapper.createObjectNode();
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

  @Test
  public void testJsonDate() {
    var datestring1 = "\"1999-12-31T23:59:58.765Z\"";
    var date = Mappers.jsonMapper.readValue(datestring1, OffsetDateTime.class);
    assertThat(date.toString(), is("1999-12-31T23:59:58.765Z"));
    var datestring2 = Mappers.jsonMapper.writeValueAsString(date);
    assertThat(datestring2, is(datestring1));
  }

  @JsonRootName(value = "test")
  public static class TestObject {

    @JsonProperty(value = "a")
    private String a;
    @JsonProperty(value = "b")
    private String b;

    public TestObject(@JsonProperty(value = "a") String a, @JsonProperty(value = "b") String b) {
      this.a = a;
      this.b = b;
    }
  }

}
